package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.naveenapps.expensemanager.core.common.utils.fromMonthAndYearKey
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYearKey
import com.naveenapps.expensemanager.core.common.utils.toYear
import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.model.BudgetTitle
import java.util.Date

/**
 * The budget's own name when it has one, otherwise an automatic one from its period: "This
 * month's budget" / "This year's budget" for the current period, else the month or year.
 */
fun budgetTitle(
    name: String?,
    selectedMonth: String,
    periodType: BudgetPeriod = BudgetPeriod.MONTHLY,
    now: Date = Date(),
): BudgetTitle {
    name?.trim()?.takeIf { it.isNotEmpty() }?.let { return BudgetTitle.Custom(it) }
    return when (periodType) {
        BudgetPeriod.YEARLY -> if (selectedMonth == now.toYear()) {
            BudgetTitle.ThisYear
        } else {
            BudgetTitle.Period(selectedMonth)
        }

        BudgetPeriod.MONTHLY -> if (selectedMonth == now.toMonthAndYearKey()) {
            BudgetTitle.ThisMonth
        } else {
            selectedMonth.fromMonthAndYearKey()?.let { BudgetTitle.Month(it) } ?: BudgetTitle.Period(selectedMonth)
        }
    }
}

fun Budget.title(now: Date = Date()): BudgetTitle = budgetTitle(name, selectedMonth, periodType, now)
