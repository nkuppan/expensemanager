package com.nkuppan.expensemanager.presentation.dashboard

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.ui.components.AppCardView
import com.nkuppan.expensemanager.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.ui.utils.AppPreviewsLightAndDarkMode
import com.nkuppan.expensemanager.ui.utils.UiText

@Composable
fun IncomeExpenseBalanceView(
    amountUiState: AmountUiState,
    modifier: Modifier = Modifier,
    showBalance: Boolean = false,
) {
    NewAmountInfoWidget(
        modifier = modifier,
        expenseAmount = amountUiState.expense,
        incomeAmount = amountUiState.income,
        balanceAmount = amountUiState.balance,
        showBalance = showBalance
    )
}

@Composable
fun ColorIconAmountView(
    @ColorRes color: Int?,
    @DrawableRes icon: Int?,
    amount: UiText,
    modifier: Modifier = Modifier,
    title: String
) {

    val context = LocalContext.current

    Row(modifier = modifier) {
        if (icon != null && color != null) {
            Icon(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .background(
                        color = colorResource(id = color),
                        shape = CircleShape
                    )
                    .size(16.dp)
                    .padding(2.dp)
                    .align(Alignment.CenterVertically),
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondary
            )
        } else {
            Spacer(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(16.dp)
                    .padding(2.dp)
                    .align(Alignment.CenterVertically)
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
            color = if (color != null) {
                colorResource(id = color)
            } else {
                Color.Unspecified
            }
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
            WidgetHeader(
                title = stringResource(id = R.string.transaction_summary),
                subTitle = transactionPeriod.asString(context)
            )

            ColorIconAmountView(
                color = R.color.red_500,
                icon = R.drawable.ic_arrow_upward,
                amount = expenseAmount,
                modifier = Modifier
                    .fillMaxWidth(),
                title = stringResource(id = R.string.spending)
            )
            ColorIconAmountView(
                color = R.color.green_500,
                icon = R.drawable.ic_arrow_downward,
                amount = incomeAmount,
                modifier = Modifier
                    .fillMaxWidth(),
                title = stringResource(id = R.string.income)
            )
            Divider()
            ColorIconAmountView(
                color = null,
                icon = null,
                amount = balanceAmount,
                modifier = Modifier
                    .fillMaxWidth(),
                title = stringResource(id = R.string.total)
            )
        }
    }
}

@Composable
fun WidgetHeader(
    title: String,
    subTitle: String,
    modifier: Modifier = Modifier
) {
    Row(modifier) {
        Text(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.CenterVertically),
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 4.dp, end = 4.dp),
            text = "-",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically),
            text = subTitle,
            style = MaterialTheme.typography.bodySmall
        )
    }
}


@Composable
fun NewAmountInfoWidget(
    modifier: Modifier,
    expenseAmount: UiText,
    incomeAmount: UiText,
    balanceAmount: UiText,
    showBalance: Boolean = false
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        NewColorIconAmountView(
            amount = incomeAmount,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            title = stringResource(id = R.string.income),
            colorResource = R.color.green_500
        )
        NewColorIconAmountView(
            amount = expenseAmount,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            title = stringResource(id = R.string.spending),
            colorResource = R.color.red_500
        )
        if (showBalance) {
            NewColorIconAmountView(
                amount = balanceAmount,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                title = stringResource(id = R.string.balance),
                colorResource = R.color.black_100
            )
        }
    }
}

@Composable
fun NewColorIconAmountView(
    amount: UiText,
    title: String,
    @ColorRes colorResource: Int,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    Surface(
        modifier = modifier,
        color = colorResource(id = colorResource).copy(alpha = .1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(top = 4.dp),
                text = amount.asString(context),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
}

private const val AMOUNT_VALUE = "100000.0$"

@AppPreviewsLightAndDarkMode
@Composable
private fun AmountViewPreview() {
    ExpenseManagerTheme {
        AmountInfoWidget(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            expenseAmount = UiText.DynamicString(AMOUNT_VALUE),
            incomeAmount = UiText.DynamicString(AMOUNT_VALUE),
            balanceAmount = UiText.DynamicString("0.00 $"),
            transactionPeriod = UiText.DynamicString("This Month(Oct 2023)"),
        )
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun NewAmountViewPreview() {
    ExpenseManagerTheme {
        NewAmountInfoWidget(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            expenseAmount = UiText.DynamicString(AMOUNT_VALUE),
            incomeAmount = UiText.DynamicString(AMOUNT_VALUE),
            balanceAmount = UiText.DynamicString("0.00 $"),
        )
    }
}