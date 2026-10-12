package com.naveenapps.expensemanager.feature.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.components.SelectedItemView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ClickableTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.SafeModalBottomSheet
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.ExportFileType
import com.naveenapps.expensemanager.feature.account.selection.MultipleAccountSelectionScreen
import com.naveenapps.expensemanager.feature.export.components.ExportFileTypeSelectionView
import com.naveenapps.expensemanager.feature.filter.datefilter.DateFilterSelectionView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// The system file picker (Storage Access Framework) works on every supported API level and
// needs no storage permission. The old fallback wrote to the shared external storage root,
// which fails on Android 10+ where WRITE_EXTERNAL_STORAGE no longer applies.
private fun getFileCreateIntent(fileType: ExportFileType): Intent {
    val timestamp = System.currentTimeMillis()
    return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        if (fileType == ExportFileType.PDF) {
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "expense_manager_$timestamp.pdf")
        } else {
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "expense_manager_$timestamp.csv")
        }
    }
}

@Composable
fun ExportScreen(
    viewModel: ExportViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    ExportScreenContent(
        state = state,
        onAction = viewModel::processAction,
        eventFlow = viewModel.event,
        shareWithUri = { uri ->
            if (uri != null) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "*/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, null))
            }
        },
    )
}

private fun createFileEvent(
    fileCreatorIntent: ManagedActivityResultLauncher<Intent, ActivityResult>,
    fileType: ExportFileType,
) {
    fileCreatorIntent.launch(getFileCreateIntent(fileType))
}

private fun fileExportedProcess(
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    event: ExportEvent.FileExported,
    context: Context,
    shareWithUri: (Uri?) -> Unit,
) {
    coroutineScope.launch {
        when (
            snackbarHostState.showSnackbar(
                message = event.message.asString(context),
                actionLabel = context.getString(R.string.share),
                withDismissAction = true,
            )
        ) {
            SnackbarResult.Dismissed -> Unit

            SnackbarResult.ActionPerformed -> {
                val uri = event.exportData.uri?.toUri()
                val file = event.exportData.file
                when {
                    uri != null -> shareWithUri(uri)

                    file != null -> shareWithUri(
                        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportScreenContent(
    state: ExportState,
    eventFlow: Flow<ExportEvent>,
    onAction: (ExportAction) -> Unit,
    shareWithUri: (Uri?) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val fileCreatorIntent = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        it.data?.data?.also { uri ->
            onAction(ExportAction.StartExport(uri.toString()))
        }
    }

    ObserveAsEvents(eventFlow) { event ->
        when (event) {
            ExportEvent.CreateFile -> createFileEvent(fileCreatorIntent, state.fileType)

            is ExportEvent.FileExported -> fileExportedProcess(coroutineScope, snackbarHostState, event, context, shareWithUri)

            is ExportEvent.Error -> coroutineScope.launch {
                snackbarHostState.showSnackbar(message = event.message.asString(context))
            }
        }
    }

    if (state.showAccountSelection) {
        SafeModalBottomSheet(
            onDismissRequest = { onAction(ExportAction.CloseAccountSelection) },
        ) {
            MultipleAccountSelectionScreen { selectedAccounts, isAllSelected ->
                onAction(ExportAction.AccountSelection(selectedAccounts, isAllSelected))
            }
        }
    }

    // Stable lambda references — prevents child composables from recomposing when the
    // parent recomposes for unrelated state changes (e.g. loading indicator).
    val currentOnAction by rememberUpdatedState(onAction)
    val onNavigateBack = remember { { currentOnAction(ExportAction.ClosePage) } }
    val onExportFileTypeChange = remember<(ExportFileType) -> Unit> {
        { fileType -> currentOnAction(ExportAction.ChangeFileType(fileType)) }
    }
    val openAccountSelection = remember { { currentOnAction(ExportAction.OpenAccountSelection) } }

    // Resolved here so the inner composable receives stable String params and can skip
    // recomposition when date/account text hasn't changed.
    val selectedDateText = state.selectedDateRangeText.asString(context)
    val accountCountText = state.accountCount.asString(context)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = onNavigateBack,
                title = null,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,

                onClick = {
                    fileCreatorIntent.launch(getFileCreateIntent(fileType = state.fileType))
                },
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp)
                            .align(Alignment.CenterVertically),
                        text = stringResource(id = R.string.export).uppercase(),
                    )
                }
            }
        },
    ) { innerPadding ->
        ExportScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            selectedDate = selectedDateText,
            exportFileType = state.fileType,
            accountCount = accountCountText,
            onExportFileTypeChange = onExportFileTypeChange,
            openAccountSelection = openAccountSelection,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportScreenContent(
    selectedDate: String,
    exportFileType: ExportFileType,
    accountCount: String,
    onExportFileTypeChange: (ExportFileType) -> Unit,
    openAccountSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    var showBottomSheet by remember { mutableStateOf(false) }

    if (showBottomSheet) {
        SafeModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
        ) {
            DateFilterSelectionView(
                onComplete = { showBottomSheet = false },
            )
        }
    }

    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
            text = stringResource(id = R.string.export),
            style = MaterialTheme.typography.displaySmall,
        )

        ExportFileTypeSelectionView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            selectedExportFileType = exportFileType,
            onExportFileTypeChange = onExportFileTypeChange,
        )

        ClickableTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            value = selectedDate,
            label = R.string.select_range,
            leadingIcon = Icons.Default.EditCalendar,
            onClick = {
                focusManager.clearFocus(force = true)
                showBottomSheet = true
            },
        )

        Spacer(modifier = Modifier.height(8.dp))

        SelectedItemView(
            modifier = Modifier
                .clickable { openAccountSelection() }
                .padding(16.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.select_account),
            icon = Icons.Outlined.AccountBalance,
            selectedCount = accountCount,
        )
    }
}
