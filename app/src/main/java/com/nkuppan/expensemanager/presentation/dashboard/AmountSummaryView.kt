package com.nkuppan.expensemanager.presentation.dashboard

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.common.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.common.ui.theme.widget.AppCardView
import com.nkuppan.expensemanager.common.ui.utils.UiText
import com.nkuppan.expensemanager.common.utils.AppPreviewsLightAndDarkMode

@Composable
fun IncomeExpenseBalanceView(
    incomeAmount: UiText,
    expenseAmount: UiText,
    balanceAmount: UiText,
    transactionPeriod: UiText,
) {
    AmountInfoWidget(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        expenseAmount = expenseAmount,
        incomeAmount = incomeAmount,
        balanceAmount = balanceAmount,
        transactionPeriod = transactionPeriod
    )
}

@Composable
fun AmountView(
    @ColorRes color: Int,
    @DrawableRes icon: Int?,
    amount: UiText,
    modifier: Modifier = Modifier,
    title: String
) {

    val context = LocalContext.current

    Row(modifier = modifier) {
        if (icon != null) {
            Icon(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .background(
                        color = colorResource(id = color),
                        shape = CircleShape
                    )
                    .size(16.dp)
                    .align(Alignment.CenterVertically),
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondary
            )
        }
        Text(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            text = title,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 8.dp)
                .align(Alignment.CenterVertically),
            text = amount.asString(context),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun AmountInfoWidget(
    modifier: Modifier,
    expenseAmount: UiText,
    incomeAmount: UiText,
    balanceAmount: UiText,
    transactionPeriod: UiText,
) {

    val context = LocalContext.current

    AppCardView(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row {
                Column {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.transaction_summary),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = transactionPeriod.asString(context),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            AmountView(
                color = R.color.red_500,
                icon = R.drawable.ic_arrow_upward,
                amount = expenseAmount,
                modifier = Modifier
                    .fillMaxWidth(),
                title = stringResource(id = R.string.expense)
            )
            AmountView(
                color = R.color.green_500,
                icon = R.drawable.ic_arrow_downward,
                amount = incomeAmount,
                modifier = Modifier
                    .fillMaxWidth(),
                title = stringResource(id = R.string.income)
            )
            Divider()
            AmountView(
                color = R.color.green_500,
                icon = null,
                amount = balanceAmount,
                modifier = Modifier
                    .fillMaxWidth(),
                title = stringResource(id = R.string.total)
            )
        }
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun AmountViewPreview() {
    ExpenseManagerTheme {
        AmountInfoWidget(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            expenseAmount = UiText.DynamicString("100.00$"),
            incomeAmount = UiText.DynamicString("100.00$"),
            balanceAmount = UiText.DynamicString("0.00$"),
            transactionPeriod = UiText.DynamicString("This Month(Oct 2023)"),
        )
    }
}