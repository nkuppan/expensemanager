package com.naveenapps.expensemanager.feature.settings.advanced

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Reorder
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBar
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.ItemSpecModifier
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.repository.BackupRepository
import com.naveenapps.expensemanager.feature.filter.datefilter.DateFilterSelectionView
import com.naveenapps.expensemanager.feature.settings.R
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun AdvancedSettingsScreen(
    backupRepository: BackupRepository,
    viewModel: AdvancedSettingsViewModel = hiltViewModel(),
) {
    val accounts by viewModel.accounts.collectAsState()
    val selectedAccount by viewModel.selectedAccount.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val selectedExpenseCategory by viewModel.selectedExpenseCategory.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val selectedIncomeCategory by viewModel.selectedIncomeCategory.collectAsState()

    AdvancedSettingsScaffoldView(
        accounts = accounts,
        selectedAccount = selectedAccount,
        expenseCategories = expenseCategories,
        selectedExpenseCategory = selectedExpenseCategory,
        incomeCategories = incomeCategories,
        selectedIncomeCategory = selectedIncomeCategory,
        onItemSelection = viewModel::onItemSelection,
        backup = {
            backupRepository.backupData(null)
        },
        restore = {
            backupRepository.restoreData(null)
        },
        accountsReOrder = viewModel::openAccountsReOrder,
        backPress = viewModel::closePage,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdvancedSettingsScaffoldView(
    accounts: List<Account>,
    selectedAccount: Account?,
    expenseCategories: List<Category>,
    selectedExpenseCategory: Category?,
    incomeCategories: List<Category>,
    selectedIncomeCategory: Category?,
    onItemSelection: (Any) -> Unit,
    backup: () -> Unit,
    restore: () -> Unit,
    accountsReOrder: () -> Unit,
    backPress: () -> Unit,
) {

    val scope = rememberCoroutineScope()
    var showDateFilter by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showDateFilter) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    showDateFilter = false
                    bottomSheetState.hide()
                }
            },
            sheetState = bottomSheetState,
            windowInsets = WindowInsets(0.dp),
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 0.dp,
        ) {
            DateFilterSelectionView(
                onComplete = {
                    scope.launch {
                        showDateFilter = false
                        bottomSheetState.hide()
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopNavigationBar(
                onClick = backPress,
                title = stringResource(R.string.advanced),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                modifier = Modifier.then(ItemSpecModifier),
                text = stringResource(id = R.string.default_selected_items),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            if (accounts.isNotEmpty() && selectedAccount != null) {
                AccountPreSelectionView(
                    accounts = accounts,
                    selectedAccount = selectedAccount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    onItemSelection = onItemSelection,
                )
            }
            if (expenseCategories.isNotEmpty() && selectedExpenseCategory != null) {
                CategoryPreSelectionView(
                    expenseCategories,
                    selectedExpenseCategory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    label = R.string.default_expense_category,
                    onItemSelection = onItemSelection,
                )
            }
            if (incomeCategories.isNotEmpty() && selectedIncomeCategory != null) {
                CategoryPreSelectionView(
                    incomeCategories,
                    selectedIncomeCategory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    label = R.string.default_income_category,
                    onItemSelection = onItemSelection,
                )
            }

            Text(
                modifier = Modifier.then(ItemSpecModifier),
                text = stringResource(id = R.string.restore_and_backup),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
            )

            SettingsItem(
                modifier = Modifier
                    .clickable {
                        backup.invoke()
                    }
                    .padding(top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                title = stringResource(id = R.string.backup),
                description = stringResource(id = R.string.backup_message),
                imageVector = Icons.Outlined.Backup,
            )

            SettingsItem(
                modifier = Modifier
                    .clickable {
                        restore.invoke()
                    }
                    .padding(top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                title = stringResource(id = R.string.restore),
                description = stringResource(id = R.string.restore_message),
                imageVector = Icons.Outlined.Restore,
            )

            Text(
                modifier = Modifier.then(ItemSpecModifier),
                text = stringResource(id = R.string.others),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
            )

            SettingsItem(
                modifier = Modifier
                    .clickable {
                        scope.launch {
                            bottomSheetState.show()
                            showDateFilter = true
                        }
                    }
                    .padding(top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                title = stringResource(id = R.string.filter),
                description = stringResource(id = R.string.filter_message),
                imageVector = Icons.Outlined.FilterAlt,
            )

            SettingsItem(
                modifier = Modifier
                    .clickable {
                        accountsReOrder.invoke()
                    }
                    .padding(top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                title = stringResource(id = R.string.accounts_re_order),
                description = stringResource(id = R.string.accounts_re_order_message),
                imageVector = Icons.Outlined.Reorder,
            )
        }
    }
}


@Composable
private fun SettingsItem(
    title: String,
    description: String,
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        Icon(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp),
            imageVector = imageVector,
            contentDescription = null,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically),
        ) {
            Text(text = title)
            Text(text = description, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AccountPreSelectionView(
    accounts: List<Account>,
    selectedAccount: Account,
    modifier: Modifier = Modifier,
    onItemSelection: ((Any) -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = {
            expanded = expanded.not()
        },
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            value = selectedAccount.name,
            onValueChange = { },
            label = {
                Text(stringResource(id = R.string.default_account))
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                )
            },
        )
        ExposedDropdownMenu(
            modifier = Modifier
                .fillMaxWidth(),
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
        ) {
            accounts.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(text = item.name)
                    },
                    onClick = {
                        onItemSelection?.invoke(item)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CategoryPreSelectionView(
    categories: List<Category>,
    selectedCategory: Category,
    @StringRes label: Int,
    modifier: Modifier = Modifier,
    onItemSelection: ((Any) -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = {
            expanded = expanded.not()
        },
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            value = selectedCategory.name,
            onValueChange = { },
            label = {
                Text(stringResource(id = label))
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                )
            },
        )
        ExposedDropdownMenu(
            modifier = Modifier
                .fillMaxWidth(),
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
        ) {
            categories.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(text = item.name)
                    },
                    onClick = {
                        onItemSelection?.invoke(item)
                        expanded = false
                    },
                )
            }
        }
    }
}

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
    ExpenseManagerTheme {
        AdvancedSettingsScaffoldView(
            accounts = getRandomAccountData(5),
            selectedAccount = getRandomAccountData(5).firstOrNull(),
            expenseCategories = getRandomCategoryData(5),
            selectedExpenseCategory = getRandomCategoryData(5).firstOrNull(),
            incomeCategories = getRandomCategoryData(5),
            selectedIncomeCategory = getRandomCategoryData(5).firstOrNull(),
            onItemSelection = {},
            backup = {},
            restore = {},
            accountsReOrder = {}
        ) {}
    }
}
