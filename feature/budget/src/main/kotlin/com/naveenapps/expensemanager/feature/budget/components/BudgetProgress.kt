package com.naveenapps.expensemanager.feature.budget.components

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUiModel
import com.naveenapps.expensemanager.core.foundation.monthAndYearKeyToLocalDate
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.model.BudgetStatus
import com.naveenapps.expensemanager.core.model.BudgetTitle
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** A budget's progress through its period, as the gauge ([BudgetGauge]) shows it. */
@Stable
data class BudgetProgress(
    val id: String,
    val title: BudgetTitle,
    val periodType: BudgetPeriod,
    val spent: String,
    val limit: String,
    /** Left to spend, or how much it's over when [isOverBudget]. */
    val remaining: String,
    val isOverBudget: Boolean,
    /** Share of the limit spent, 0..1 (capped at 1 when over). */
    val progress: Float,
    val status: BudgetStatus,
    /** Per day for the rest of the period; null when the period is over or the budget is spent. */
    val safePerDay: String?,
    /** First day of the period, used to name it ("October", "2026"). */
    val periodStart: LocalDate,
)

/** Works out what's left and the daily allowance as of [today]; null for an unreadable period. */
fun BudgetUiModel.toBudgetProgress(
    today: LocalDate,
    format: (Double) -> String,
): BudgetProgress? {
    val (start, end) = when (periodType) {
        BudgetPeriod.MONTHLY -> {
            val first = selectedMonth.monthAndYearKeyToLocalDate() ?: return null
            first to first.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        }

        BudgetPeriod.YEARLY -> {
            val year = selectedMonth.toIntOrNull() ?: return null
            LocalDate(year, 1, 1) to LocalDate(year, 12, 31)
        }
    }
    val limit = amount.amount
    val spent = transactionAmount.amount
    val left = limit - spent
    val isOver = left < 0
    // Days still to come in the period, today included; none once it has ended.
    val from = maxOf(today, start)
    val daysLeft = if (from > end) 0L else from.daysUntil(end) + 1L
    return BudgetProgress(
        id = id,
        title = title,
        periodType = periodType,
        spent = transactionAmount.amountString.orEmpty(),
        limit = amount.amountString.orEmpty(),
        remaining = format(if (isOver) -left else left),
        isOverBudget = isOver,
        progress = if (limit > 0) (spent / limit).toFloat().coerceIn(0f, 1f) else 1f,
        status = status,
        safePerDay = if (daysLeft > 0 && left > 0) format(left / daysLeft) else null,
        periodStart = start,
    )
}
