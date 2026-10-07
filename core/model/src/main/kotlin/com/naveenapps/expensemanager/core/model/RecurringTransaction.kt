package com.naveenapps.expensemanager.core.model

import java.util.Date

enum class RecurringFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
}

/**
 * A repeating transaction: the template every occurrence is copied from, and when the next one
 * is due. Occurrence n falls on [startDate] + n × [frequency] (computed from the start, so a
 * rule on the 31st lands on the last day of shorter months and goes back to the 31st after).
 * The original transaction the user saved is occurrence 0.
 */
data class RecurringTransaction(
    val id: String,
    val notes: String,
    val categoryId: String,
    val fromAccountId: String,
    val toAccountId: String?,
    val type: TransactionType,
    val amount: Double,
    val frequency: RecurringFrequency,
    val startDate: Date,
    /** Number of the next occurrence to create (1 after the first save). */
    val nextOccurrence: Int,
    val nextDueDate: Date,
    val createdOn: Date,
    val updatedOn: Date,
    // Filled in for display only.
    val category: Category? = null,
    val fromAccount: Account? = null,
    val toAccount: Account? = null,
)
