package com.naveenapps.expensemanager.feature.transaction.recurring

import com.naveenapps.expensemanager.core.model.RecurringTransaction

data class RecurringTransactionsState(
    /** Rules matching [searchQuery]; null until loaded, so the empty state doesn't flash in. */
    val items: List<RecurringItemUi>? = null,
    val searchQuery: String = "",
    /** True when there are rules at all, so "no matches" can be told apart from "nothing yet". */
    val hasAnyRules: Boolean = false,
    val pendingStop: RecurringItemUi? = null,
)

data class RecurringItemUi(
    val rule: RecurringTransaction,
    val amount: String,
)
