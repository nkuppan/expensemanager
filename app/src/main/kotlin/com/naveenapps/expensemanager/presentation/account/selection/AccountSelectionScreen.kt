package com.naveenapps.expensemanager.presentation.account.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.presentation.account.list.ACCOUNT_DUMMY_DATA
import com.naveenapps.expensemanager.presentation.account.list.AccountItem
import com.naveenapps.expensemanager.presentation.account.list.AccountUiModel
import com.naveenapps.expensemanager.presentation.selection.SelectionTitle
import com.naveenapps.expensemanager.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.ui.utils.ItemSpecModifier

@Composable
fun AccountSelectionScreen(
    modifier: Modifier = Modifier,
    accounts: List<AccountUiModel> = emptyList(),
    selectedAccount: AccountUiModel? = null,
    onItemSelection: ((AccountUiModel) -> Unit)? = null
) {

    val context = LocalContext.current

    LazyColumn(modifier = modifier) {
        item {
            SelectionTitle(stringResource(id = R.string.select_account))
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
                                    color = colorResource(id = R.color.green_500).copy(alpha = .1f),
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
                    icon = account.icon,
                    iconBackgroundColor = account.iconBackgroundColor,
                    amount = account.amount.asString(context),
                    endIcon = if (isSelected) {
                        R.drawable.ic_done
                    } else {
                        null
                    }
                )
            }
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