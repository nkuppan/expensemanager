package com.nkuppan.expensemanager.presentation.transaction.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.TransactionType
import com.nkuppan.expensemanager.domain.model.isExpense
import com.nkuppan.expensemanager.domain.model.isIncome
import com.nkuppan.expensemanager.domain.model.isTransfer
import com.nkuppan.expensemanager.ui.components.AppFilterChip
import com.nkuppan.expensemanager.ui.theme.ExpenseManagerTheme


@Composable
fun TransactionTypeSelectionView(
    selectedTransactionType: TransactionType,
    onTransactionTypeChange: ((TransactionType) -> Unit),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppFilterChip(
            modifier = Modifier.align(Alignment.CenterVertically),
            filterName = stringResource(id = R.string.income),
            isSelected = selectedTransactionType.isIncome(),
            filterIcon = R.drawable.ic_arrow_downward,
            filterSelectedColor = R.color.green_500,
            onClick = {
                onTransactionTypeChange.invoke(TransactionType.INCOME)
            }
        )
        AppFilterChip(
            modifier = Modifier.align(Alignment.CenterVertically),
            filterName = stringResource(id = R.string.spending),
            isSelected = selectedTransactionType.isExpense(),
            filterIcon = R.drawable.ic_arrow_upward,
            filterSelectedColor = R.color.red_500,
            onClick = {
                onTransactionTypeChange.invoke(TransactionType.EXPENSE)
            }
        )
        AppFilterChip(
            modifier = Modifier.align(Alignment.CenterVertically),
            filterName = stringResource(id = R.string.transfer),
            isSelected = selectedTransactionType.isTransfer(),
            filterIcon = R.drawable.ic_transfer,
            filterSelectedColor = R.color.blue_500,
            onClick = {
                onTransactionTypeChange.invoke(TransactionType.TRANSFER)
            }
        )
    }
}

@Preview
@Composable
private fun TransactionTypeSelectionViewPreview() {
    ExpenseManagerTheme {
        Column {
            TransactionTypeSelectionView(
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                selectedTransactionType = TransactionType.INCOME,
                onTransactionTypeChange = {}
            )
            TransactionTypeSelectionView(
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                selectedTransactionType = TransactionType.EXPENSE,
                onTransactionTypeChange = {}
            )
            TransactionTypeSelectionView(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                selectedTransactionType = TransactionType.TRANSFER,
                onTransactionTypeChange = {}
            )
        }
    }
}
