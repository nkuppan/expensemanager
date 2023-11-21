package com.naveenapps.expensemanager.feature.account.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.ui.components.SelectionTitle
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.ItemSpecModifier
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getSelectedBGColor
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.feature.account.R
import com.naveenapps.expensemanager.feature.account.list.ACCOUNT_DUMMY_DATA
import com.naveenapps.expensemanager.feature.account.list.AccountItem

@Composable
fun AccountSelectionScreen(
    modifier: Modifier = Modifier,
    accounts: List<AccountUiModel> = emptyList(),
    selectedAccount: AccountUiModel? = null,
    onItemSelection: ((AccountUiModel) -> Unit)? = null
) {
    LazyColumn(modifier = modifier) {
        item {
            SelectionTitle(
                stringResource(id = R.string.select_account), Modifier.Companion
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 4.dp)
            )
        }
        items(accounts) { account ->
            val isSelected = selectedAccount?.id == account.id
            Box(
                modifier = Modifier
                    .clickable {
                        onItemSelection?.invoke(account)
                    }
                    .then(
                        if (isSelected) {
                            Modifier
                                .padding(4.dp)
                                .background(
                                    color = getSelectedBGColor(),
                                    shape = RoundedCornerShape(size = 12.dp)
                                )
                        } else {
                            Modifier
                                .padding(4.dp)
                        }
                    )
                    .then(ItemSpecModifier)
            ) {
                AccountItem(
                    modifier = Modifier
                        .clickable {
                            onItemSelection?.invoke(account)
                        },
                    name = account.name,
                    icon = account.storedIcon.name,
                    iconBackgroundColor = account.storedIcon.backgroundColor,
                    amount = account.amount.amountString,
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}


@Preview
@Composable
private fun AccountSelectionScreenPreview() {
    ExpenseManagerTheme {
        AccountSelectionScreen(
            accounts = ACCOUNT_DUMMY_DATA,
            selectedAccount = ACCOUNT_DUMMY_DATA.firstOrNull()
        )
    }
}