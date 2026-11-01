package com.naveenapps.expensemanager.feature.dashboard

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUiModel
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.feature.budget.components.BudgetProgress
import java.time.Year
import java.util.Date

/**
 * Every monthly budget for the month being viewed and every yearly budget for this year, largest
 * limit first. Home shows one card per budget, swipeable when a period has more than one.
 */
@Stable
data class HomeBudgets(
    val monthly: List<BudgetProgress> = emptyList(),
    val yearly: List<BudgetProgress> = emptyList(),
    /** The month being viewed (for "No budget for October 2026 yet"); null outside a month view. */
    val month: Date? = null,
    val year: String = Year.now().toString(),
)

/**
 * Picks what Home shows: every monthly budget for [activeMonth] and every yearly budget for
 * [currentYear]. Each list is ordered largest limit first, so the overall budget is normally the
 * first card and the narrower ones (a category, an account) follow.
 */
internal fun pickHomeBudgets(
    budgets: List<BudgetUiModel>,
    activeMonth: String?,
    currentYear: String,
): Pair<List<BudgetUiModel>, List<BudgetUiModel>> {
    val monthly = activeMonth?.let { month ->
        budgets.filter { it.periodType == BudgetPeriod.MONTHLY && it.selectedMonth == month }
            .sortedByDescending { it.amount.amount }
    }.orEmpty()
    val yearly = budgets.filter { it.periodType == BudgetPeriod.YEARLY && it.selectedMonth == currentYear }
        .sortedByDescending { it.amount.amount }
    return monthly to yearly
}
