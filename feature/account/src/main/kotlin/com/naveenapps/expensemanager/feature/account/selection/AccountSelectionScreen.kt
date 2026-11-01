package com.naveenapps.expensemanager.feature.account.selection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.components.SelectionHeader
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.NoSearchResults
import com.naveenapps.expensemanager.core.designsystem.ui.components.SheetSearchField
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.feature.account.R
import com.naveenapps.expensemanager.feature.account.list.getRandomAccountUiModel

@Composable
fun AccountSelectionScreen(
    modifier: Modifier = Modifier,
    accounts: List<AccountUiModel> = emptyList(),
    selectedAccount: AccountUiModel? = null,
    createNewCallback: (() -> Unit)? = null,
    onItemSelection: ((AccountUiModel) -> Unit)? = null,
) {
    // Same search as the budget screen's account sheet. Cleared each time the sheet opens.
    var query by rememberSaveable { mutableStateOf("") }
    val visibleAccounts = remember(accounts, query) {
        val search = query.trim()
        if (search.isEmpty()) accounts else accounts.filter { it.name.contains(search, ignoreCase = true) }
    }

    Column(modifier = modifier) {
        SelectionHeader(
            title = stringResource(id = R.string.select_account),
            createNewCallback = createNewCallback,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        SheetSearchField(
            query = query,
            onQueryChange = { query = it },
            placeholder = stringResource(id = R.string.search_accounts),
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        if (visibleAccounts.isEmpty() && query.isNotBlank()) {
            NoSearchResults(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp))
        }

        // The header and search stay put while the list scrolls under them.
        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(visibleAccounts, key = { it.id }) { account ->
                val isSelected = selectedAccount?.id == account.id
                AccountItem(
                    onClick = {
                        onItemSelection?.invoke(account)
                    },
                    name = account.name,
                    icon = account.storedIcon.name,
                    iconBackgroundColor = account.storedIcon.backgroundColor,
                    amount = account.amount.amountString,
                    amountTextColor = account.amountTextColor,
                    customImagePath = account.storedIcon.customImagePath,
                    border = AccountItemDefaults.border(isSelected),
                    trailingContent = {
                        AccountItemDefaults.SingleCheckedTrailing(isSelected)
                    },
                )
            }
            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Preview
@Composable
private fun AccountSelectionScreenPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        val accounts = getRandomAccountUiModel(10)
        AccountSelectionScreen(
            accounts = accounts,
            selectedAccount = accounts.firstOrNull(),
            onItemSelection = {},
            createNewCallback = {},
        )
    }
}
