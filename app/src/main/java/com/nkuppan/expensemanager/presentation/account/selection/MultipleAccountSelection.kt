package com.nkuppan.expensemanager.presentation.account.selection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.hilt.navigation.compose.hiltViewModel
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.common.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.presentation.account.list.AccountCheckedItem
import com.nkuppan.expensemanager.presentation.account.list.AccountUiModel
import com.nkuppan.expensemanager.presentation.account.list.DUMMY_DATA
import com.nkuppan.expensemanager.presentation.selection.SelectionTitle


@Composable
fun MultipleAccountSelectionScreen(
    onItemSelection: ((List<AccountUiModel>) -> Unit)? = null
) {

    val viewModel: AccountSelectionViewModel = hiltViewModel()
    val accounts by viewModel.accounts.collectAsState()
    val selectedAccounts by viewModel.selectedAccounts.collectAsState()

    MultipleAccountSelectionScreen(
        modifier = Modifier,
        accounts = accounts,
        selectedAccounts = selectedAccounts,
        onApplyChanges = {
            onItemSelection?.invoke(selectedAccounts)
        },
        onClearChanges = viewModel::clearChanges,
        onItemSelection = viewModel::selectThisAccount
    )
}

@Composable
fun MultipleAccountSelectionScreen(
    modifier: Modifier = Modifier,
    accounts: List<AccountUiModel>,
    selectedAccounts: List<AccountUiModel>,
    onApplyChanges: (() -> Unit),
    onClearChanges: (() -> Unit),
    onItemSelection: ((AccountUiModel, Boolean) -> Unit)? = null
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SelectionTitle(stringResource(id = R.string.select_account))
        }
        items(accounts) { account ->
            val isSelected = selectedAccounts.fastAny { it.id == account.id }
            AccountCheckedItem(
                modifier = Modifier
                    .clickable {
                        onItemSelection?.invoke(account, isSelected.not())
                    }
                    .padding(start = 16.dp, end = 16.dp),
                name = account.name,
                icon = account.icon,
                iconBackgroundColor = account.iconBackgroundColor,
                isSelected = isSelected,
                onCheckedChange = {
                    onItemSelection?.invoke(account, it)
                }
            )
        }
        item {
            Column {
                Divider(modifier = Modifier.padding(top = 8.dp))
                Row(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                ) {
                    TextButton(onClick = onClearChanges) {
                        Text(text = stringResource(id = R.string.clear_all).uppercase())
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(onClick = onApplyChanges) {
                        Text(text = stringResource(id = R.string.select).uppercase())
                    }
                }
                Spacer(modifier = Modifier.padding(8.dp))
            }
        }
    }
}


@Preview
@Composable
private fun MultipleAccountSelectionScreenPreview() {
    ExpenseManagerTheme {
        MultipleAccountSelectionScreen(
            accounts = DUMMY_DATA,
            selectedAccounts = DUMMY_DATA,
            onApplyChanges = {},
            onClearChanges = {},
            onItemSelection = null
        )
    }
}