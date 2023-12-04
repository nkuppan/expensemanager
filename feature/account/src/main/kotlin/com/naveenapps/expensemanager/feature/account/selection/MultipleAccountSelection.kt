package com.naveenapps.expensemanager.feature.account.selection

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.designsystem.ui.components.SelectionTitle
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.feature.account.R
import com.naveenapps.expensemanager.feature.account.list.AccountCheckedItem
import com.naveenapps.expensemanager.feature.account.list.getRandomAccountUiModel

@Composable
fun MultipleAccountSelectionScreen(
    viewModel: AccountSelectionViewModel = hiltViewModel(),
    selectedAccounts: List<AccountUiModel> = emptyList(),
    onItemSelection: ((List<AccountUiModel>, Boolean) -> Unit)? = null,
) {
    viewModel.selectAllThisAccount(selectedAccounts)

    AccountSelectionView(viewModel, onItemSelection)
}

@Composable
private fun AccountSelectionView(
    viewModel: AccountSelectionViewModel,
    onItemSelection: ((List<AccountUiModel>, Boolean) -> Unit)?,
) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsState()
    val selectedAccounts by viewModel.selectedAccounts.collectAsState()

    MultipleAccountSelectionScreen(
        modifier = Modifier,
        accounts = accounts,
        selectedAccounts = selectedAccounts,
        onApplyChanges = {
            if (selectedAccounts.isNotEmpty()) {
                onItemSelection?.invoke(
                    selectedAccounts,
                    selectedAccounts.size == accounts.size,
                )
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.account_selection_message),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        },
        onClearChanges = viewModel::clearChanges,
        onItemSelection = viewModel::selectThisAccount,
    )
}

@Composable
fun MultipleAccountSelectionScreen(
    modifier: Modifier = Modifier,
    accounts: List<AccountUiModel>,
    selectedAccounts: List<AccountUiModel>,
    onApplyChanges: (() -> Unit),
    onClearChanges: (() -> Unit),
    onItemSelection: ((AccountUiModel, Boolean) -> Unit)? = null,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            SelectionTitle(
                stringResource(id = R.string.select_account),
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
            )
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
                icon = account.storedIcon.name,
                iconBackgroundColor = account.storedIcon.backgroundColor,
                isSelected = isSelected,
                onCheckedChange = {
                    onItemSelection?.invoke(account, it)
                },
            )
        }
        item {
            Column {
                Divider(modifier = Modifier.padding(top = 8.dp))
                Row(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .fillMaxWidth(),
                ) {
                    TextButton(onClick = onClearChanges) {
                        Text(text = stringResource(id = R.string.clear_all).uppercase())
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(onClick = onApplyChanges) {
                        Text(text = stringResource(id = R.string.select).uppercase())
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Preview
@Composable
private fun MultipleAccountSelectionScreenPreview() {
    ExpenseManagerTheme {
        val accounts = getRandomAccountUiModel(10)
        MultipleAccountSelectionScreen(
            accounts = accounts,
            selectedAccounts = accounts,
            onApplyChanges = {},
            onClearChanges = {},
            onItemSelection = null,
        )
    }
}
