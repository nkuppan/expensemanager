package com.naveenapps.expensemanager.feature.transaction.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.em.EmSegmented
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.feature.transaction.R

@Composable
fun TransactionTypeSelectionView(
    selectedTransactionType: TransactionType,
    onTransactionTypeChange: ((TransactionType) -> Unit),
    modifier: Modifier = Modifier,
) {
    val types = listOf(
        TransactionType.INCOME,
        TransactionType.EXPENSE,
        TransactionType.TRANSFER,
        TransactionType.REFUND,
    )
    val colors = EmTheme.colors
    EmSegmented(
        options = listOf(
            stringResource(id = R.string.income),
            stringResource(id = R.string.expense),
            stringResource(id = R.string.transfer),
            stringResource(id = R.string.refund),
        ),
        selectedIndex = types.indexOf(selectedTransactionType),
        onSelect = { onTransactionTypeChange(types[it]) },
        modifier = modifier,
        icons = listOf(
            Icons.AutoMirrored.Rounded.TrendingUp,
            Icons.AutoMirrored.Rounded.TrendingDown,
            Icons.Rounded.SwapHoriz,
            Icons.AutoMirrored.Rounded.Undo, // money coming back to the user
        ),
        accents = listOf(colors.income, colors.expense, colors.transfer, colors.refund),
    )
}

@Preview
@Composable
private fun TransactionTypeSelectionViewPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        Column {
            TransactionTypeSelectionView(
                modifier = Modifier
                    .padding(top = EmTheme.spacing.lg, start = EmTheme.spacing.lg, end = EmTheme.spacing.lg)
                    .fillMaxWidth(),
                selectedTransactionType = TransactionType.INCOME,
                onTransactionTypeChange = {},
            )
            TransactionTypeSelectionView(
                modifier = Modifier
                    .padding(top = EmTheme.spacing.lg, start = EmTheme.spacing.lg, end = EmTheme.spacing.lg)
                    .fillMaxWidth(),
                selectedTransactionType = TransactionType.EXPENSE,
                onTransactionTypeChange = {},
            )
            TransactionTypeSelectionView(
                modifier = Modifier
                    .padding(EmTheme.spacing.lg)
                    .fillMaxWidth(),
                selectedTransactionType = TransactionType.TRANSFER,
                onTransactionTypeChange = {},
            )
        }
    }
}
