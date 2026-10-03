package com.naveenapps.expensemanager.core.model

/** The selected Analysis period against the one before it (see ComparisonWindows in core:domain). */
data class PeriodComparison(
    val type: DateRangeType,
    /** Both periods were cut at the same point because the current one is still in progress. */
    val isToDate: Boolean,
    /** Window bounds in epoch millis, end exclusive. */
    val currentStart: Long,
    val currentEnd: Long,
    val previousStart: Long,
    val previousEnd: Long,
    val currentExpense: Double,
    val previousExpense: Double,
    val currentIncome: Double,
    val previousIncome: Double,
    /** Expense categories whose spending changed most, biggest absolute change first. */
    val categoryChanges: List<CategoryChange>,
)

data class CategoryChange(
    val category: Category,
    val current: Double,
    val previous: Double,
) {
    val difference: Double get() = current - previous
}

/** Relative change from [previous] to [current], or null when there's nothing to compare against. */
fun changeRatio(current: Double, previous: Double): Double? =
    if (previous > 0.0) (current - previous) / previous else null
