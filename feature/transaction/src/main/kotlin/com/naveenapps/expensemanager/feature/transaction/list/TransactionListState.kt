package com.naveenapps.expensemanager.feature.transaction.list

data class TransactionListState(
    val transactionListItem: List<TransactionListItem>,
    val searchQuery: String = "",
    // True until the first result arrives, so the empty state doesn't flash on open.
    val isLoading: Boolean = false,
    /** Which dates a search covers. Searching defaults to all time. */
    val searchScope: SearchScope = SearchScope.ALL_TIME,
    /** Count and totals of what a search found; null while not searching. */
    val searchSummary: SearchSummary? = null,
)

enum class SearchScope {
    /** Every transaction, whatever the selected period. */
    ALL_TIME,

    /** Only the period selected in the filter above the list. */
    SELECTED_PERIOD,
}

/** "12 results · Spent ₹4,320 · Earned ₹1,000". A total is null when it's zero. */
data class SearchSummary(
    val count: Int,
    val spent: String?,
    val earned: String?,
)
