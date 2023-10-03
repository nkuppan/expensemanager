package com.nkuppan.expensemanager.presentation.transaction.create

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.common.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.domain.model.TransactionType


@Composable
fun TransactionTypeSelectionView(
    selectedTransactionType: TransactionType,
    onTransactionTypeChange: ((TransactionType) -> Unit),
    modifier: Modifier = Modifier
) {

    Row(modifier = modifier) {
        CustomCategoryFilterChip(
            selectedTransactionType = selectedTransactionType,
            transactionType = TransactionType.INCOME,
            filterName = stringResource(id = R.string.income),
            filterIcon = R.drawable.ic_arrow_downward,
            filterSelectedColor = R.color.green_500,
            onTransactionTypeChange = onTransactionTypeChange,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        CustomCategoryFilterChip(
            selectedTransactionType = selectedTransactionType,
            transactionType = TransactionType.EXPENSE,
            filterName = stringResource(id = R.string.expense),
            filterIcon = R.drawable.ic_arrow_upward,
            filterSelectedColor = R.color.red_500,
            onTransactionTypeChange = onTransactionTypeChange,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 16.dp)
        )
        CustomCategoryFilterChip(
            selectedTransactionType = selectedTransactionType,
            transactionType = TransactionType.TRANSFER,
            filterName = stringResource(id = R.string.transfer),
            filterIcon = R.drawable.ic_transfer,
            filterSelectedColor = R.color.blue_500,
            onTransactionTypeChange = onTransactionTypeChange,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 16.dp)
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun RowScope.CustomCategoryFilterChip(
    selectedTransactionType: TransactionType,
    transactionType: TransactionType,
    filterName: String,
    @DrawableRes filterIcon: Int,
    @ColorRes filterSelectedColor: Int,
    onTransactionTypeChange: ((TransactionType) -> Unit),
    modifier: Modifier = Modifier,
) {
    FilterChip(
        modifier = modifier,
        selected = selectedTransactionType == transactionType,
        onClick = {
            onTransactionTypeChange.invoke(transactionType)
        },
        label = {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = filterName
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = filterIcon),
                contentDescription = ""
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedLabelColor = Color.White,
            selectedContainerColor = colorResource(id = filterSelectedColor),
            selectedLeadingIconColor = Color.White
        )
    )
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
