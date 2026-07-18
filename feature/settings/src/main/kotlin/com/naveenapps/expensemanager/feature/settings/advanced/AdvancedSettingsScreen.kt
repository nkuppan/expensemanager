package com.naveenapps.expensemanager.feature.settings.advanced

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Reorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.SafeModalBottomSheet
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingRow
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingsSection
import com.naveenapps.expensemanager.feature.filter.datefilter.DateFilterSelectionView
import com.naveenapps.expensemanager.feature.settings.R
import org.koin.compose.viewmodel.koinViewModel

// Defaults, Data & Backup, and Security moved to the main Settings screen — they're common
// enough that hiding them here made them hard to find. What's left are the genuinely rare,
// set-once-and-forget items.
@Composable
fun AdvancedSettingsScreen(
    viewModel: AdvancedSettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    AdvancedSettingsScaffoldView(
        state = state,
        onAction = viewModel::processAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdvancedSettingsScaffoldView(
    state: AdvancedSettingState,
    onAction: (AdvancedSettingAction) -> Unit,
) {

    if (state.showDateFilter) {
        SafeModalBottomSheet(
            onDismissRequest = {
                onAction.invoke(AdvancedSettingAction.DismissDateFilterDialog)
            },
        ) {
            DateFilterSelectionView(
                onComplete = {
                    onAction.invoke(AdvancedSettingAction.DismissDateFilterDialog)
                },
            )
        }
    }

    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = {
                    onAction.invoke(AdvancedSettingAction.ClosePage)
                },
                title = stringResource(R.string.advanced),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(0.dp))

            SettingsSection(
                title = stringResource(id = R.string.others),
            ) {
                AppCardView {
                    SettingRow(
                        title = stringResource(id = R.string.filter),
                        subtitle = stringResource(id = R.string.filter_message),
                        icon = Icons.Outlined.FilterAlt,
                        showDivider = true,
                        onClick = { onAction.invoke(AdvancedSettingAction.ShowDateFilterDialog) },
                    )
                    SettingRow(
                        title = stringResource(id = R.string.accounts_re_order),
                        subtitle = stringResource(id = R.string.accounts_re_order_message),
                        icon = Icons.Outlined.Reorder,
                        onClick = { onAction.invoke(AdvancedSettingAction.OpenAccountReOrder) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview
@Composable
fun AdvancedSettingsPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        AdvancedSettingsScaffoldView(
            state = AdvancedSettingState(showDateFilter = false),
            onAction = {},
        )
    }
}
