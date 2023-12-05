package com.naveenapps.expensemanager.feature.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBar
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.StoredIcon
import java.util.Date

@Composable
fun AdvancedSettingsScreen(
    viewModel: AdvancedSettingsViewModel = hiltViewModel()
) {
    AdvancedSettingsScaffoldView(
        accounts = viewModel.accounts,
        selectedAccount = viewModel.selectedAccount,
        expenseCategories = viewModel.expenseCategories,
        selectedExpenseCategory = viewModel.selectedExpenseCategory,
        incomeCategories = viewModel.incomeCategories,
        selectedIncomeCategory = viewModel.selectedIncomeCategory,
        onItemSelection = viewModel::onItemSelection,
        backPress = viewModel::closePage,
        saveChanges = viewModel::saveChanges,
    )
}

@Composable
private fun AdvancedSettingsScaffoldView(
    accounts: List<Account>,
    selectedAccount: Account?,
    expenseCategories: List<Category>,
    selectedExpenseCategory: Category?,
    incomeCategories: List<Category>,
    selectedIncomeCategory: Category?,
    onItemSelection: (Any) -> Unit,
    backPress: () -> Unit,
    saveChanges: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopNavigationBar(
                onClick = backPress,
                title = stringResource(R.string.advanced),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = saveChanges) {
                Icon(
                    imageVector = Icons.Outlined.Done,
                    contentDescription = "",
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
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
        }
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
                    expanded = expanded
                )
            }
        )
        ExposedDropdownMenu(
            modifier = Modifier
                .fillMaxWidth(),
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            accounts.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(text = item.name)
                    },
                    onClick = {
                        onItemSelection?.invoke(item)
                        expanded = false
                    }
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
        }
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
                    expanded = expanded
                )
            }
        )
        ExposedDropdownMenu(
            modifier = Modifier
                .fillMaxWidth(),
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            categories.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(text = item.name)
                    },
                    onClick = {
                        onItemSelection?.invoke(item)
                        expanded = false
                    }
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
            saveChanges = {},
            backPress = {},
        )
    }
}