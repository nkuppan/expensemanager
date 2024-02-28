package com.naveenapps.expensemanager.feature.account.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import com.naveenapps.expensemanager.feature.account.list.AccountItem
import com.naveenapps.expensemanager.feature.account.list.getRandomAccountUiModel

@Composable
fun AccountSelectionScreen(
    modifier: Modifier = Modifier,
    accounts: List<AccountUiModel> = emptyList(),
    selectedAccount: AccountUiModel? = null,
    createNewCallback: (() -> Unit)? = null,
    onItemSelection: ((AccountUiModel) -> Unit)? = null,
) {
    LazyColumn(modifier = modifier) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                SelectionTitle(
                    title = stringResource(id = R.string.select_account),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .clickable {
                            createNewCallback?.invoke()
                        },
                    text = stringResource(id = R.string.add_new).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
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
                                    shape = RoundedCornerShape(size = 12.dp),
                                )
                        } else {
                            Modifier
                                .padding(4.dp)
                        },
                    )
                    .then(ItemSpecModifier),
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
                    amountTextColor = account.amountTextColor,
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
        val accounts = getRandomAccountUiModel(10)
        AccountSelectionScreen(
            accounts = accounts,
            selectedAccount = accounts.firstOrNull(),
        )
    }
}
