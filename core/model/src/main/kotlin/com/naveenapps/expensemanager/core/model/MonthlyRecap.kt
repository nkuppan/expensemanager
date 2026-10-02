package com.naveenapps.expensemanager.core.model

import java.util.Date

/**
 * A finished month's spending summary, used for the shareable recap.
 * Shares are fractions (0..1) of [totalExpense]; the share image shows only these, never amounts.
 */
data class MonthlyRecap(
    /** Any date inside the recapped month (the first day). */
    val month: Date,
    /** "yyyyMM" key, used to remember a dismissed recap card. */
    val monthKey: String,
    val totalExpense: Double,
    val transactionCount: Int,
    /** Distinct days in the month with at least one transaction. */
    val daysLogged: Int,
    val topCategories: List<RecapCategory>,
)

data class RecapCategory(
    val category: Category,
    val share: Float,
)
