package com.naveenapps.expensemanager.core.designsystem.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getBalanceBGColor
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getExpenseBGColor
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getIncomeBGColor


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
            title = stringResource(id = R.string.income),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            showBalance = showBalance,
            color = getIncomeBGColor()
        )
        NewColorIconAmountView(
            amount = expenseAmount,
            title = stringResource(id = R.string.expense),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            showBalance = showBalance,
            color = getExpenseBGColor()
        )
        if (showBalance) {
            NewColorIconAmountView(
                amount = balanceAmount,
                title = stringResource(id = R.string.balance),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                showBalance = showBalance,
                color = getBalanceBGColor()
            )
        }
    }
}

@Composable
fun NewColorIconAmountView(
    amount: String,
    title: String,
    modifier: Modifier = Modifier,
    showBalance: Boolean = false,
    color: Color
) {
    Surface(
        modifier = modifier,
        color = color,
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