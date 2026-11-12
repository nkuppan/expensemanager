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
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Reorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.SafeModalBottomSheet
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingRow
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingToggleRow
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingsSection
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.LandingPage
import com.naveenapps.expensemanager.feature.filter.datefilter.DateFilterSelectionView
import com.naveenapps.expensemanager.feature.settings.DropdownSettingItem
import com.naveenapps.expensemanager.feature.settings.R
import com.naveenapps.expensemanager.feature.settings.labelResId
import org.koin.compose.viewmodel.koinViewModel

// Settings → More → Preferences: defaults for new entries, Home, and the rare set-once items.
@Composable
fun AdvancedSettingsScreen(
    viewModel: AdvancedSettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AdvancedSettingsScaffoldView(
        state = state,
        onAction = viewModel::processAction,
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
                title = stringResource(R.string.preferences),
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

            // New entries start from these.
            SettingsSection(title = stringResource(id = R.string.transaction_entry)) {
                AppCardView {
                    state.selectedAccount?.takeIf { state.accounts.isNotEmpty() }?.let { selected ->
                        DropdownSettingItem(
                            label = stringResource(id = R.string.default_account),
                            selectedValue = selected.name,
                            icon = Icons.Outlined.AccountBalance,
                            items = state.accounts.map { it.name },
                            onItemSelected = { index ->
                                onAction.invoke(AdvancedSettingAction.SelectAccount(state.accounts[index]))
                            },
                        )
                    }
                    state.selectedExpenseCategory?.takeIf { state.expenseCategories.isNotEmpty() }?.let { selected ->
                        DropdownSettingItem(
                            label = stringResource(id = R.string.default_expense_category),
                            selectedValue = selected.displayName(),
                            icon = Icons.AutoMirrored.Outlined.TrendingUp,
                            items = state.expenseCategories.map { it.displayName() },
                            onItemSelected = { index ->
                                onAction.invoke(
                                    AdvancedSettingAction.SelectExpenseCategory(state.expenseCategories[index]),
                                )
                            },
                        )
                    }
                    state.selectedIncomeCategory?.takeIf { state.incomeCategories.isNotEmpty() }?.let { selected ->
                        DropdownSettingItem(
                            label = stringResource(id = R.string.default_income_category),
                            selectedValue = selected.displayName(),
                            icon = Icons.AutoMirrored.Outlined.TrendingDown,
                            items = state.incomeCategories.map { it.displayName() },
                            onItemSelected = { index ->
                                onAction.invoke(
                                    AdvancedSettingAction.SelectIncomeCategory(state.incomeCategories[index]),
                                )
                            },
                        )
                    }
                    SettingToggleRow(
                        title = stringResource(id = R.string.auto_open_calculator),
                        subtitle = stringResource(id = R.string.auto_open_calculator_message),
                        icon = Icons.Outlined.Calculate,
                        checked = state.isAutoOpenCalculator,
                        onCheckedChange = { onAction.invoke(AdvancedSettingAction.ToggleAutoOpenCalculator) },
                    )
                }
            }

            SettingsSection(title = stringResource(id = R.string.landing_home)) {
                AppCardView {
                    DropdownSettingItem(
                        label = stringResource(id = R.string.landing_page),
                        selectedValue = stringResource(id = state.landingPage.labelResId),
                        icon = Icons.Outlined.Home,
                        items = LandingPage.entries.map { stringResource(id = it.labelResId) },
                        onItemSelected = { index ->
                            onAction.invoke(AdvancedSettingAction.SelectLandingPage(LandingPage.entries[index]))
                        },
                    )
                    SettingToggleRow(
                        title = stringResource(id = R.string.compact_summary),
                        subtitle = stringResource(id = R.string.compact_summary_message),
                        icon = Icons.Outlined.DashboardCustomize,
                        checked = state.isCompactSummary,
                        onCheckedChange = { onAction.invoke(AdvancedSettingAction.ToggleCompactSummary) },
                    )
                }
            }

            SettingsSection(title = stringResource(id = R.string.others)) {
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
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        AdvancedSettingsScaffoldView(
            state = AdvancedSettingState(showDateFilter = false),
            onAction = {},
        )
    }
}

@Composable
private fun Category.displayName(): String = titleResId?.let { stringResource(it) } ?: name
