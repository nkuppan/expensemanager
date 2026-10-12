package com.naveenapps.expensemanager.feature.transaction.list

data class TransactionListState(
    val transactionListItem: List<TransactionListItem>,
    val searchQuery: String = "",
    // True until the first result arrives, so the empty state doesn't flash on open.
    val isLoading: Boolean = false,
)
