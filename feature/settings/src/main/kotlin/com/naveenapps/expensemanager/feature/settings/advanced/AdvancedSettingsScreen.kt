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
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Reorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmDivider
import com.naveenapps.expensemanager.core.designsystem.em.EmModalSheet
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSection
import com.naveenapps.expensemanager.core.designsystem.em.EmSettingItem
import com.naveenapps.expensemanager.core.designsystem.em.EmSettingTrailing
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.LandingPage
import com.naveenapps.expensemanager.feature.filter.datefilter.DateFilterSelectionView
import com.naveenapps.expensemanager.feature.settings.R
import com.naveenapps.expensemanager.feature.settings.SelectionSettingItem
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

@Composable
private fun AdvancedSettingsScaffoldView(
    state: AdvancedSettingState,
    onAction: (AdvancedSettingAction) -> Unit,
) {
    if (state.showDateFilter) {
        EmModalSheet(
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

    EmScaffold(
        topBar = {
            EmTopBar(
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = {
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
                .padding(horizontal = EmTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
        ) {
            Spacer(modifier = Modifier.height(0.dp))

            // New entries start from these.
            EmSection(title = stringResource(id = R.string.transaction_entry)) {
                EmCard {
                    state.selectedAccount?.takeIf { state.accounts.isNotEmpty() }?.let { selected ->
                        SelectionSettingItem(
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
                        SelectionSettingItem(
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
                        SelectionSettingItem(
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
                    EmSettingItem(
                        icon = Icons.Outlined.Calculate,
                        title = stringResource(id = R.string.auto_open_calculator),
                        supporting = stringResource(id = R.string.auto_open_calculator_message),
                        trailing = EmSettingTrailing.Toggle(state.isAutoOpenCalculator, { onAction.invoke(AdvancedSettingAction.ToggleAutoOpenCalculator) }),
                    )
                }
            }

            EmSection(title = stringResource(id = R.string.landing_home)) {
                EmCard {
                    SelectionSettingItem(
                        label = stringResource(id = R.string.landing_page),
                        selectedValue = stringResource(id = state.landingPage.labelResId),
                        icon = Icons.Outlined.Home,
                        items = LandingPage.entries.map { stringResource(id = it.labelResId) },
                        onItemSelected = { index ->
                            onAction.invoke(AdvancedSettingAction.SelectLandingPage(LandingPage.entries[index]))
                        },
                    )
                    EmSettingItem(
                        icon = Icons.Outlined.DashboardCustomize,
                        title = stringResource(id = R.string.compact_summary),
                        supporting = stringResource(id = R.string.compact_summary_message),
                        trailing = EmSettingTrailing.Toggle(state.isCompactSummary, { onAction.invoke(AdvancedSettingAction.ToggleCompactSummary) }),
                    )
                }
            }

            EmSection(title = stringResource(id = R.string.others)) {
                EmCard {
                    EmSettingItem(
                        icon = Icons.Outlined.FilterAlt,
                        title = stringResource(id = R.string.filter),
                        supporting = stringResource(id = R.string.filter_message),
                        onClick = { onAction.invoke(AdvancedSettingAction.ShowDateFilterDialog) },
                    )
                    EmDivider()
                    EmSettingItem(
                        icon = Icons.Outlined.Reorder,
                        title = stringResource(id = R.string.accounts_re_order),
                        supporting = stringResource(id = R.string.accounts_re_order_message),
                        onClick = { onAction.invoke(AdvancedSettingAction.OpenAccountReOrder) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(EmTheme.spacing.lg))
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
