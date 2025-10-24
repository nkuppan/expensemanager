package com.naveenapps.expensemanager.feature.export

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import org.koin.compose.viewmodel.koinViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.naveenapps.expensemanager.core.designsystem.components.SelectedItemView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ClickableTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBar
import com.naveenapps.expensemanager.core.designsystem.ui.utils.UiText
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.ExportFileType
import com.naveenapps.expensemanager.feature.account.selection.MultipleAccountSelectionScreen
import com.naveenapps.expensemanager.feature.export.components.ExportFileTypeSelectionView
import com.naveenapps.expensemanager.feature.filter.datefilter.DateFilterSelectionView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date

private fun getFileCreateIntent(fileType: ExportFileType): Intent? {
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
        Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            if (fileType == ExportFileType.PDF) {
                type = "application/pdf"
                putExtra(Intent.EXTRA_TITLE, "export_file_${Date().time}.pdf")
            } else {
                type = "application/csv"
                putExtra(Intent.EXTRA_TITLE, "export_file_${Date().time}.csv")
            }
        }
    } else {
        null
    }
}

@Composable
fun ExportScreen(
    viewModel: ExportViewModel = koinViewModel()
) {

    val state by viewModel.state.collectAsState()

    ExportScreenContent(
        state = state,
        onAction = viewModel::processAction,
        eventFlow = viewModel.event,
        shareWithUri = {}
    )
}


@OptIn(ExperimentalPermissionsApi::class)
private fun createFileEvent(
    fileCreatorIntent: ManagedActivityResultLauncher<Intent, ActivityResult>,
    writePermission: PermissionState,
    onAction: (ExportAction) -> Unit,
) {
    getFileCreateIntent(ExportFileType.CSV)?.let { intent ->
        fileCreatorIntent.launch(intent)
    } ?: run {
        val permissionResult = writePermission.status
        if (permissionResult.isGranted) {
            onAction(ExportAction.StartExport(null))
        } else {
            if (permissionResult.shouldShowRationale) {
                // TODO Show Alert to Grant Permission
            } else {
                writePermission.launchPermissionRequest()
            }
        }
    }
}


private fun fileExportedProcess(
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    event: ExportEvent.FileExported,
    context: Context,
    shareWithUri: (Uri?) -> Unit,
) {
    coroutineScope.launch {
        snackbarHostState.showSnackbar(
            message = event.message.asString(context),
            actionLabel = context.getString(R.string.share),
            withDismissAction = true,
        ).run {
            when (this) {
                SnackbarResult.Dismissed -> Unit
                SnackbarResult.ActionPerformed -> {
                    event.exportData.let {
                        val uri = it.uri?.toUri()
                        val file = it.file
                        if (uri != null) {
                            shareWithUri.invoke(uri)
                        } else if (file != null) {
                            val fileUri = FileProvider.getUriForFile(
                                context,
                                context.packageName + ".fileprovider",
                                file
                            )
                            shareWithUri.invoke(fileUri)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
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

    val writePermission = rememberPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    val fileCreatorIntent = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        it.data?.data?.also { uri ->
            onAction(ExportAction.StartExport(uri.toString()))
        }
    }

    ObserveAsEvents(eventFlow) { event ->
        when (event) {
            ExportEvent.CreateFile -> {
                createFileEvent(
                    fileCreatorIntent,
                    writePermission,
                    onAction
                )
            }

            is ExportEvent.FileExported -> {
                fileExportedProcess(
                    coroutineScope,
                    snackbarHostState,
                    event,
                    context,
                    shareWithUri
                )
            }

            is ExportEvent.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(message = event.message.asString(context))
                }
            }
        }
    }

    if (state.showAccountSelection) {
        ModalBottomSheet(
            onDismissRequest = {
                onAction.invoke(ExportAction.CloseAccountSelection)
            },
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 0.dp,
        ) {
            MultipleAccountSelectionScreen { selectedAccounts, isAllSelected ->
                onAction.invoke(
                    ExportAction.AccountSelection(
                        selectedAccounts,
                        isAllSelected
                    )
                )
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopNavigationBar(
                onClick = { onAction.invoke(ExportAction.ClosePage) },
                title = null,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    getFileCreateIntent(fileType = state.fileType)?.let {
                        fileCreatorIntent.launch(it)
                    } ?: run {
                        onAction.invoke(ExportAction.StartExport(null))
                    }
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
            selectedDate = state.selectedDateRangeText,
            exportFileType = state.fileType,
            accountCount = state.accountCount,
            onExportFileTypeChange = {
                onAction.invoke(ExportAction.ChangeFileType(it))
            },
            openAccountSelection = {
                onAction.invoke(ExportAction.OpenAccountSelection)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportScreenContent(
    selectedDate: UiText,
    exportFileType: ExportFileType,
    accountCount: UiText,
    onExportFileTypeChange: (ExportFileType) -> Unit,
    openAccountSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var showBottomSheet by remember { mutableStateOf(false) }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 0.dp,
        ) {
            DateFilterSelectionView(
                onComplete = {
                    showBottomSheet = false
                }
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
            value = selectedDate.asString(context),
            label = R.string.select_range,
            leadingIcon = Icons.Default.EditCalendar,
            onClick = {
                focusManager.clearFocus(force = true)
                showBottomSheet = true
            },
        )

        Spacer(modifier = Modifier.padding(8.dp))

        SelectedItemView(
            modifier = Modifier
                .clickable {
                    openAccountSelection.invoke()
                }
                .padding(16.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.select_account),
            icon = Icons.Outlined.AccountBalance,
            selectedCount = accountCount.asString(context),
        )
        if (exportFileType == ExportFileType.PDF) {
            Text(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                text = stringResource(id = R.string.export_disabled_message),
            )
        }
    }
}
