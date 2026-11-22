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
import com.naveenapps.expensemanager.core.designsystem.em.EmNoSearchResults
import com.naveenapps.expensemanager.core.designsystem.em.EmSearchField
import com.naveenapps.expensemanager.core.designsystem.em.EmSectionHeader
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
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
        EmSectionHeader(
            title = stringResource(id = R.string.select_account),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.sm),
            onAddClick = createNewCallback,
        )

        EmSearchField(
            query = query,
            onQueryChange = { query = it },
            placeholder = stringResource(id = R.string.search_accounts),
            modifier = Modifier.padding(horizontal = EmTheme.spacing.lg),
        )

        if (visibleAccounts.isEmpty() && query.isNotBlank()) {
            EmNoSearchResults(modifier = Modifier.padding(horizontal = EmTheme.spacing.xl, vertical = EmTheme.spacing.xl))
        }

        // The header and search stay put while the list scrolls under them.
        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            contentPadding = PaddingValues(
                start = EmTheme.spacing.lg,
                end = EmTheme.spacing.lg,
                top = EmTheme.spacing.md,
            ),
            verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
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
                    amountTone = account.amountTone,
                    customImagePath = account.storedIcon.customImagePath,
                    selected = isSelected,
                    trailingContent = {
                        AccountItemDefaults.SingleCheckedTrailing(isSelected)
                    },
                )
            }
            item {
                Spacer(modifier = Modifier.height(EmTheme.spacing.xxxl))
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
