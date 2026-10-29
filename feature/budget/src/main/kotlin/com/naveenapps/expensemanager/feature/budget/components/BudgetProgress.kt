package com.naveenapps.expensemanager.feature.budget.components

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.common.utils.fromMonthAndYearKey
import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUiModel
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.model.BudgetTitle
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit

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
    val progressBarColor: Int,
    /** Per day for the rest of the period; null when the period is over or the budget is spent. */
    val safePerDay: String?,
    /** First day of the period, used to name it ("October", "2026"). */
    val periodStart: LocalDate,
)

/** Works out what's left and the daily allowance as of [today]; null for an unreadable period. */
fun BudgetUiModel.toBudgetProgress(
    today: LocalDate,
    format: (Double) -> String,
    zone: ZoneId = ZoneId.systemDefault(),
): BudgetProgress? {
    val (start, end) = when (periodType) {
        BudgetPeriod.MONTHLY -> {
            val month = selectedMonth.fromMonthAndYearKey()
                ?.let { YearMonth.from(it.toInstant().atZone(zone)) }
                ?: return null
            month.atDay(1) to month.atEndOfMonth()
        }

        BudgetPeriod.YEARLY -> {
            val year = selectedMonth.toIntOrNull()?.let { Year.of(it) } ?: return null
            year.atDay(1) to year.atMonth(12).atEndOfMonth()
        }
    }
    val limit = amount.amount
    val spent = transactionAmount.amount
    val left = limit - spent
    val isOver = left < 0
    // Days still to come in the period, today included; none once it has ended.
    val from = maxOf(today, start)
    val daysLeft = if (from.isAfter(end)) 0L else ChronoUnit.DAYS.between(from, end) + 1
    return BudgetProgress(
        id = id,
        title = title,
        periodType = periodType,
        spent = transactionAmount.amountString.orEmpty(),
        limit = amount.amountString.orEmpty(),
        remaining = format(if (isOver) -left else left),
        isOverBudget = isOver,
        progress = if (limit > 0) (spent / limit).toFloat().coerceIn(0f, 1f) else 1f,
        progressBarColor = progressBarColor,
        safePerDay = if (daysLeft > 0 && left > 0) format(left / daysLeft) else null,
        periodStart = start,
    )
}
