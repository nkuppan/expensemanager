package com.naveenapps.expensemanager.feature.dashboard

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUiModel
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.feature.budget.components.BudgetProgress
import java.time.Year
import java.util.Date

/** The one monthly and one yearly budget Home shows, each as a gauge card. */
@Stable
data class HomeBudgets(
    val monthly: BudgetProgress? = null,
    val yearly: BudgetProgress? = null,
    /** The month being viewed (for "No budget for October 2026 yet"); null outside a month view. */
    val month: Date? = null,
    val year: String = Year.now().toString(),
)

/**
 * Picks what Home shows: the monthly budget for [activeMonth] and the yearly budget for
 * [currentYear]. With several for the same period, the largest limit wins, as that's normally
 * the overall budget (the rest stay in "View all").
 */
internal fun pickHomeBudgets(
    budgets: List<BudgetUiModel>,
    activeMonth: String?,
    currentYear: String,
): Pair<BudgetUiModel?, BudgetUiModel?> {
    val monthly = activeMonth?.let { month ->
        budgets.filter { it.periodType == BudgetPeriod.MONTHLY && it.selectedMonth == month }
            .maxByOrNull { it.amount.amount }
    }
    val yearly = budgets.filter { it.periodType == BudgetPeriod.YEARLY && it.selectedMonth == currentYear }
        .maxByOrNull { it.amount.amount }
    return monthly to yearly
}
