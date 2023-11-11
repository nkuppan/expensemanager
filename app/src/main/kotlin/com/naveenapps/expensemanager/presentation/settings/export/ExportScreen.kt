package com.naveenapps.expensemanager.presentation.settings.export

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.core.common.utils.toCompleteDate
import com.naveenapps.expensemanager.core.designsystem.ui.components.ClickableTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBar
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.shareThisFile
import com.naveenapps.expensemanager.core.designsystem.ui.utils.UiText
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.ExportFileType
import com.naveenapps.expensemanager.presentation.account.selection.MultipleAccountSelectionScreen
import com.naveenapps.expensemanager.presentation.budget.create.SelectedItemView
import com.naveenapps.expensemanager.presentation.settings.datefilter.DateFilterSelectionView
import kotlinx.coroutines.launch
import java.util.Date

private fun createFile(fileType: ExportFileType): Intent? {
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


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ExportScreen(navController: NavController) {

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val writePermission = rememberPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    LaunchedEffect(writePermission) {
        val permissionResult = writePermission.status
        if (permissionResult.isGranted.not()) {
            if (permissionResult.shouldShowRationale) {
                //TODO Show Alert to Grant Permission
            } else {
                writePermission.launchPermissionRequest()
            }
        }
    }

    val viewModel: ExportViewModel = hiltViewModel()
    val selectedDateRange by viewModel.selectedDateRange.collectAsState()
    val exportFileType by viewModel.exportFileType.collectAsState()
    val accountCount by viewModel.accountCount.collectAsState()

    val success by viewModel.success.collectAsState(null)
    val error by viewModel.error.collectAsState(null)

    val fileCreatorIntent = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode != RESULT_OK) {
            return@rememberLauncherForActivityResult
        }

        it.data?.data?.also { uri ->
            viewModel.export(uri)
        }
    }

    if (success != null) {
        LaunchedEffect(key1 = "completed", block = {
            snackbarHostState.showSnackbar(
                message = success?.message?.asString(context) ?: "",
                actionLabel = context.getString(R.string.share),
                withDismissAction = true
            ).run {
                when (this) {
                    SnackbarResult.Dismissed -> Unit
                    SnackbarResult.ActionPerformed -> {
                        success?.fileUri?.let {
                            context.shareThisFile(it)
                        }
                    }
                }
            }
        })
    }

    if (error != null) {
        LaunchedEffect(key1 = "error", block = {
            snackbarHostState.showSnackbar(error?.asString(context) ?: "")
        })
    }

    ExportScreenScaffoldView(
        navController = navController,
        selectedDateRange = selectedDateRange,
        exportFileType = exportFileType,
        accountCount = accountCount,
        onExportFileTypeChange = viewModel::setExportFileType,
        onExport = {
            if (exportFileType != ExportFileType.PDF) {
                createFile(fileType = exportFileType)?.let {
                    fileCreatorIntent.launch(it)
                } ?: run {
                    viewModel.export(null)
                }
            }
        },
        setAccounts = viewModel::setAccounts,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportScreenScaffoldView(
    navController: NavController,
    selectedDateRange: String?,
    exportFileType: ExportFileType,
    accountCount: UiText,
    onExportFileTypeChange: (ExportFileType) -> Unit,
    onExport: () -> Unit,
    setAccounts: (List<AccountUiModel>, Boolean) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val scope = rememberCoroutineScope()

    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = bottomSheetState,
            windowInsets = WindowInsets(0.dp)
        ) {
            MultipleAccountSelectionScreen { items, selected ->
                setAccounts.invoke(items, selected)
                scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                    if (!bottomSheetState.isVisible) {
                        showBottomSheet = false
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopNavigationBar(
                navController = navController, title = null
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onExport,
            ) {
                Row {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_export),
                        contentDescription = null
                    )
                    Text(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp)
                            .align(Alignment.CenterVertically),
                        text = stringResource(id = R.string.export).uppercase()
                    )
                }
            }
        }
    ) { innerPadding ->
        ExportScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            selectedDateRange,
            exportFileType,
            accountCount,
            onExportFileTypeChange,
            openAccountSelection = {
                scope.launch {
                    if (bottomSheetState.isVisible) {
                        bottomSheetState.hide()
                    } else {
                        showBottomSheet = true
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportScreenContent(
    modifier: Modifier = Modifier,
    selectedDate: String? = Date().toCompleteDate(),
    exportFileType: ExportFileType,
    accountCount: UiText,
    onExportFileTypeChange: (ExportFileType) -> Unit,
    openAccountSelection: () -> Unit
) {

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = bottomSheetState,
            windowInsets = WindowInsets(0.dp)
        ) {
            DateFilterSelectionView {
                showBottomSheet = false
            }
        }
    }

    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
            text = stringResource(id = R.string.export),
            style = MaterialTheme.typography.displaySmall
        )

        ExportFileTypeSelectionView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            selectedExportFileType = exportFileType,
            onExportFileTypeChange = onExportFileTypeChange
        )

        ClickableTextField(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            value = selectedDate ?: "",
            label = R.string.select_range,
            leadingIcon = R.drawable.ic_calendar,
            onClick = {
                focusManager.clearFocus(force = true)
                showBottomSheet = true
            })

        Spacer(modifier = Modifier.padding(8.dp))

        SelectedItemView(
            modifier = Modifier
                .clickable {
                    openAccountSelection.invoke()
                }
                .padding(16.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.select_account),
            icon = painterResource(id = com.naveenapps.expensemanager.core.designsystem.R.drawable.savings),
            selectedCount = accountCount.asString(context)
        )
        if (exportFileType == ExportFileType.PDF) {
            Text(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                text = stringResource(id = R.string.export_disabled_message)
            )
        }
    }
}