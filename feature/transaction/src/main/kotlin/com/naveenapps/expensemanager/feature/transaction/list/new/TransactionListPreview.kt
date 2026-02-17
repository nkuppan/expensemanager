package com.naveenapps.expensemanager.feature.transaction.list.new

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme

@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 900,
    name = "Transaction List – Dark",
)
@Composable
fun TransactionListScreenPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        TransactionListScreen(
            transactions = SampleTransactions,
            onAddClick = {},
            onTransactionClick = {},
            onEditClick = {},
            onDeleteClick = {},
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 900,
    name = "Transaction List – Empty",
)
@Composable
fun TransactionListScreenEmptyPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        TransactionListScreen(
            transactions = emptyList(),
            onAddClick = {},
            onTransactionClick = {},
            onEditClick = {},
            onDeleteClick = {},
        )
    }
}
