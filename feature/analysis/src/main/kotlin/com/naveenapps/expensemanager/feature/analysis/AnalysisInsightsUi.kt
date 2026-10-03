package com.naveenapps.expensemanager.feature.analysis

import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import java.time.DayOfWeek

/** [com.naveenapps.expensemanager.core.model.SpendingInsights] with amounts formatted for display. */
data class AnalysisInsightsUi(
    val hasExpenses: Boolean,
    val savingsRate: Float?,
    val categories: List<CategoryRowUi>,
    val weekdays: List<WeekdayBarUi>,
    val busiestWeekday: DayOfWeek?,
    /** Busiest weekday's share of all spending (0..1), for the highlight text. */
    val busiestWeekdayShare: Float,
    val biggestExpenses: List<TransactionUiItem>,
)

data class CategoryRowUi(
    val category: Category,
    val amount: String,
    val share: Float,
    val count: Int,
)

data class WeekdayBarUi(
    val dayOfWeek: DayOfWeek,
    val amount: String,
    /** Bar height relative to the busiest weekday (0..1). */
    val relative: Float,
)

/** [com.naveenapps.expensemanager.core.model.PeriodComparison] formatted for display. */
data class PeriodComparisonUi(
    val titleResId: Int,
    /** e.g. "1–3 Oct vs 1–3 Sep". */
    val periodsLabel: String,
    val expense: ComparisonMetricUi,
    val income: ComparisonMetricUi,
    val categoryChanges: List<CategoryChangeUi>,
)

data class ComparisonMetricUi(
    val current: String,
    val previous: String,
    /** Relative change, or null when the previous period had nothing. */
    val change: Double?,
)

data class CategoryChangeUi(
    val category: Category,
    val current: String,
    val previous: String,
    /** Signed, e.g. "+₹300" / "−₹50". */
    val difference: String,
    val isIncrease: Boolean,
)
