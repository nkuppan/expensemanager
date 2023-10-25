package com.nkuppan.expensemanager.presentation.settings.export

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.data.utils.toTransactionDate
import com.nkuppan.expensemanager.domain.model.ExportFileType
import com.nkuppan.expensemanager.presentation.budget.create.SelectedItemView
import com.nkuppan.expensemanager.presentation.settings.datefilter.DateFilterView
import com.nkuppan.expensemanager.ui.theme.widget.ClickableTextField
import com.nkuppan.expensemanager.ui.theme.widget.TopNavigationBar
import java.util.Date


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ExportScreen(navController: NavController) {
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
    ExportScreenScaffoldView(
        navController = navController,
        selectedDateRange = selectedDateRange,
        exportFileType = exportFileType,
        onExportFileTypeChange = viewModel::setExportFileType,
        onExport = viewModel::export,
    )
}

@Composable
private fun ExportScreenScaffoldView(
    navController: NavController,
    selectedDateRange: String?,
    exportFileType: ExportFileType,
    onExportFileTypeChange: ((ExportFileType) -> Unit),
    onExport: (() -> Unit),
) {
    Scaffold(topBar = {
        TopNavigationBar(
            navController = navController, title = null
        )
    }, floatingActionButton = {
        ExtendedFloatingActionButton(onClick = onExport) {
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
    }) { innerPadding ->
        ExportScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            selectedDateRange,
            exportFileType,
            onExportFileTypeChange
        )
    }
}

@Composable
private fun ExportScreenContent(
    modifier: Modifier = Modifier,
    selectedDate: String? = Date().toTransactionDate(),
    exportFileType: ExportFileType,
    onExportFileTypeChange: ((ExportFileType) -> Unit)
) {

    val focusManager = LocalFocusManager.current

    var showDateFilter by remember { mutableStateOf(false) }
    if (showDateFilter) {
        DateFilterView {
            showDateFilter = false
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
                showDateFilter = true
            })

        Spacer(modifier = Modifier.padding(8.dp))

        SelectedItemView(modifier = Modifier
            .clickable {
                //openAccountSelection?.invoke()
            }
            .padding(16.dp)
            .fillMaxWidth(),
            title = stringResource(id = R.string.select_account),
            icon = painterResource(id = R.drawable.savings),
            selectedCount = stringResource(id = R.string.all))
    }
}