package com.naveenapps.expensemanager.feature.account.selection

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.SelectionTitle
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.feature.account.R
import com.naveenapps.expensemanager.feature.account.list.getRandomAccountUiModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MultipleAccountSelectionScreen(
    viewModel: AccountSelectionViewModel = koinViewModel(),
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
    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.select_account),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            if (selectedAccounts.isNotEmpty()) {
                Text(
                    text = pluralStringResource(
                        id = R.plurals.items_selected,
                        count = selectedAccounts.size,
                        selectedAccounts.size,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        // Account list
        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(accounts, key = { it.id }) { account ->
                val isSelected = selectedAccounts.fastAny { it.id == account.id }
                AccountItem(
                    name = account.name,
                    icon = account.storedIcon.name,
                    iconBackgroundColor = account.storedIcon.backgroundColor,
                    amount = account.amount.amountString,
                    amountTextColor = account.amountTextColor,
                    border = AccountItemDefaults.border(isSelected),
                    onClick = {
                        onItemSelection?.invoke(account, isSelected.not())
                    },
                    trailingContent = {
                        AccountItemDefaults.MultiCheckedTrailing(isSelected)
                    },
                )
            }
        }

        // Bottom actions
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onClearChanges,
                enabled = selectedAccounts.isNotEmpty(),
            ) {
                Text(
                    text = stringResource(id = R.string.clear_all),
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onApplyChanges) {
                Text(
                    text = stringResource(id = R.string.apply),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Preview
@Composable
private fun MultipleAccountSelectionScreenPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        val accounts = getRandomAccountUiModel(10)
        MultipleAccountSelectionScreen(
            accounts = accounts,
            selectedAccounts = listOf(accounts[0], accounts[1]),
            onApplyChanges = {},
            onClearChanges = {},
            onItemSelection = null,
        )
    }
}
