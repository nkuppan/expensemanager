package com.naveenapps.expensemanager.feature.transaction.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Transform
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppFilterChip
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.model.isIncome
import com.naveenapps.expensemanager.core.model.isTransfer
import com.naveenapps.expensemanager.feature.transaction.R


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
            filterIcon = Icons.Default.ArrowDownward,
            filterSelectedColor = com.naveenapps.expensemanager.core.common.R.color.green_500,
            onClick = {
                onTransactionTypeChange.invoke(TransactionType.INCOME)
            }
        )
        AppFilterChip(
            modifier = Modifier.align(Alignment.CenterVertically),
            filterName = stringResource(id = R.string.spending),
            isSelected = selectedTransactionType.isExpense(),
            filterIcon = Icons.Default.ArrowUpward,
            filterSelectedColor = com.naveenapps.expensemanager.core.common.R.color.red_500,
            onClick = {
                onTransactionTypeChange.invoke(TransactionType.EXPENSE)
            }
        )
        AppFilterChip(
            modifier = Modifier.align(Alignment.CenterVertically),
            filterName = stringResource(id = R.string.transfer),
            isSelected = selectedTransactionType.isTransfer(),
            filterIcon = Icons.Default.Transform,
            filterSelectedColor = com.naveenapps.expensemanager.core.common.R.color.blue_500,
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
