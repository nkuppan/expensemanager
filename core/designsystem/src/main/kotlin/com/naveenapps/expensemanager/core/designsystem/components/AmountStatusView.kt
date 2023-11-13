package com.naveenapps.expensemanager.core.designsystem.components

import androidx.annotation.ColorRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme


@Composable
fun AmountStatusView(
    modifier: Modifier,
    expenseAmount: String,
    incomeAmount: String,
    balanceAmount: String,
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
            colorResource = com.naveenapps.expensemanager.core.common.R.color.green_500,
            showBalance = showBalance
        )
        NewColorIconAmountView(
            amount = expenseAmount,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            title = stringResource(id = R.string.spending),
            colorResource = com.naveenapps.expensemanager.core.common.R.color.red_500,
            showBalance = showBalance
        )
        if (showBalance) {
            NewColorIconAmountView(
                amount = balanceAmount,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                title = stringResource(id = R.string.balance),
                colorResource = com.naveenapps.expensemanager.core.common.R.color.black_100,
                showBalance = showBalance
            )
        }
    }
}

@Composable
fun NewColorIconAmountView(
    amount: String,
    title: String,
    @ColorRes colorResource: Int,
    modifier: Modifier = Modifier,
    showBalance: Boolean = false
) {
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
                style = if (showBalance)
                    MaterialTheme.typography.bodySmall
                else
                    MaterialTheme.typography.titleMedium,
            )
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(top = 4.dp),
                text = amount,
                fontWeight = FontWeight.Bold,
                style = if (showBalance)
                    MaterialTheme.typography.bodySmall
                else
                    MaterialTheme.typography.headlineSmall,
            )
        }
    }
}

const val AMOUNT_VALUE = "10000000.0$"

@Preview
@Composable
private fun AmountStatusViewPreview() {
    ExpenseManagerTheme {
        Column {
            AmountStatusView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                expenseAmount = AMOUNT_VALUE,
                incomeAmount = AMOUNT_VALUE,
                balanceAmount = AMOUNT_VALUE,
            )
            AmountStatusView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                expenseAmount = AMOUNT_VALUE,
                incomeAmount = AMOUNT_VALUE,
                balanceAmount = AMOUNT_VALUE,
                showBalance = true
            )
        }
    }
}