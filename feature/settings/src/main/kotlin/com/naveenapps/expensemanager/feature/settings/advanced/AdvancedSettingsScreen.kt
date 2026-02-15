package com.naveenapps.expensemanager.feature.settings.advanced

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Reorder
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingRow
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.repository.BackupRepository
import com.naveenapps.expensemanager.feature.filter.datefilter.DateFilterSelectionView
import com.naveenapps.expensemanager.feature.settings.R
import org.koin.compose.viewmodel.koinViewModel
import java.util.Date

@Composable
fun AdvancedSettingsScreen(
    backupRepository: BackupRepository,
    viewModel: AdvancedSettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    ObserveAsEvents(viewModel.event) {
        when (it) {
            AdvancedSettingEvent.Backup -> {
                backupRepository.backupData(null)
            }

            AdvancedSettingEvent.Restore -> {
                backupRepository.restoreData(null)
            }
        }
    }

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
        ModalBottomSheet(
            onDismissRequest = {
                onAction.invoke(AdvancedSettingAction.DismissDateFilterDialog)
            },
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 0.dp,
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

            // Default selections
            SettingsSection(
                title = stringResource(id = R.string.default_selected_items),
            ) {
                AppCardView {
                    if (state.accounts.isNotEmpty() && state.selectedAccount != null) {
                        DropdownSettingItem(
                            label = stringResource(id = R.string.default_account),
                            selectedValue = state.selectedAccount.name,
                            icon = Icons.Outlined.AccountBalance,
                            items = state.accounts.map { it.name },
                            onItemSelected = { index ->
                                onAction.invoke(AdvancedSettingAction.SelectAccount(state.accounts[index]))
                            },
                        )
                    }

                    if (state.expenseCategories.isNotEmpty() && state.selectedExpenseCategory != null) {
                        DropdownSettingItem(
                            label = stringResource(id = R.string.default_expense_category),
                            selectedValue = state.selectedExpenseCategory.name,
                            icon = Icons.AutoMirrored.Outlined.TrendingUp,
                            items = state.expenseCategories.map { it.name },
                            onItemSelected = { index ->
                                onAction.invoke(AdvancedSettingAction.SelectExpenseCategory(state.expenseCategories[index]))
                            },
                        )
                    }

                    if (state.incomeCategories.isNotEmpty() && state.selectedIncomeCategory != null) {
                        DropdownSettingItem(
                            label = stringResource(id = R.string.default_income_category),
                            selectedValue = state.selectedIncomeCategory.name,
                            icon = Icons.AutoMirrored.Outlined.TrendingDown,
                            items = state.incomeCategories.map { it.name },
                            onItemSelected = { index ->
                                onAction.invoke(AdvancedSettingAction.SelectIncomeCategory(state.incomeCategories[index]))
                            },
                        )
                    }
                }
            }

            // Backup & Restore
            SettingsSection(
                title = stringResource(id = R.string.restore_and_backup),
            ) {
                AppCardView {
                    SettingRow(
                        title = stringResource(id = R.string.backup),
                        subtitle = stringResource(id = R.string.backup_message),
                        icon = Icons.Outlined.Backup,
                        showDivider = true,
                        onClick = { onAction.invoke(AdvancedSettingAction.Backup) },
                    )
                    SettingRow(
                        title = stringResource(id = R.string.restore),
                        subtitle = stringResource(id = R.string.restore_message),
                        icon = Icons.Outlined.Restore,
                        onClick = { onAction.invoke(AdvancedSettingAction.Restore) },
                    )
                }
            }

            // Others
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

// ─── Reusable Components ────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSettingItem(
    label: String,
    selectedValue: String,
    icon: ImageVector,
    items: List<String>,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier.fillMaxWidth(),
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = selectedValue,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = MaterialTheme.shapes.medium,
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item,
                            fontWeight = if (item == selectedValue) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    leadingIcon = if (item == selectedValue) {
                        {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    } else null,
                    onClick = {
                        onItemSelected(index)
                        expanded = false
                    },
                )
            }
        }
    }
}

// ─── Preview helpers (unchanged) ────────────────────────────────────────────────

fun getRandomAccountData(totalCount: Int = 10): List<Account> {
    return buildList {
        repeat(totalCount) {
            add(getAccountData(it))
        }
    }
}

fun getRandomCategoryData(totalCount: Int = 10): List<Category> {
    return buildList {
        repeat(totalCount) {
            add(getCategoryData(it))
        }
    }
}

fun getAccountData(
    index: Int,
    accountType: AccountType = AccountType.CREDIT,
    amount: Double = 0.0,
): Account {
    return Account(
        id = "$index",
        name = "Account $index",
        type = accountType,
        storedIcon = StoredIcon(
            name = "credit_card",
            backgroundColor = "#000000",
        ),
        amount = amount,
        createdOn = Date(),
        updatedOn = Date(),
    )
}

fun getCategoryData(
    index: Int,
    categoryType: CategoryType = CategoryType.EXPENSE,
): Category {
    return Category(
        id = "$index",
        name = "Account $index",
        type = categoryType,
        storedIcon = StoredIcon(
            name = "credit_card",
            backgroundColor = "#000000",
        ),
        createdOn = Date(),
        updatedOn = Date(),
    )
}

@Preview
@Composable
fun AdvancedSettingsPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        AdvancedSettingsScaffoldView(
            state = AdvancedSettingState(
                accounts = getRandomAccountData(5),
                selectedAccount = getRandomAccountData(5).firstOrNull(),
                expenseCategories = getRandomCategoryData(5),
                selectedExpenseCategory = getRandomCategoryData(5).firstOrNull(),
                incomeCategories = getRandomCategoryData(5),
                selectedIncomeCategory = getRandomCategoryData(5).firstOrNull(),
                showDateFilter = false,
            ),
            onAction = {},
        )
    }
}
