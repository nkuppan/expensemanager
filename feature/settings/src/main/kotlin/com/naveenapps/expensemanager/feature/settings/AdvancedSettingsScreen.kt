package com.naveenapps.expensemanager.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
        categories = viewModel.categories,
        backPress = viewModel::closePage,
    )
}

@Composable
private fun AdvancedSettingsScaffoldView(
    accounts: List<Account>,
    categories: List<Category>,
    backPress: () -> Unit
) {
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
                .verticalScroll(rememberScrollState())
        ) {
            if (accounts.isNotEmpty()) {
                AccountPreSelectionView(
                    accounts, modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
            if (categories.isNotEmpty()) {
                CategoryPreSelectionView(
                    categories,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AccountPreSelectionView(
    accounts: List<Account>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedAccount by remember { mutableStateOf(accounts[0]) }

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
                Text(stringResource(id = R.string.default_selected_account))
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
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = {
                        Text(text = account.name)
                    },
                    onClick = {
                        selectedAccount = account
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
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedAccount by remember { mutableStateOf(categories[0]) }

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
                Text(stringResource(id = R.string.default_selected_category))
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
            categories.forEach { account ->
                DropdownMenuItem(
                    text = {
                        Text(text = account.name)
                    },
                    onClick = {
                        selectedAccount = account
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
            categories = getRandomCategoryData(5),
        ) {

        }
    }
}