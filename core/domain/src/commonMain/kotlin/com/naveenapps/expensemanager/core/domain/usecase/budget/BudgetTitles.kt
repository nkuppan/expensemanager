package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.naveenapps.expensemanager.core.foundation.monthAndYearKeyToLocalDate
import com.naveenapps.expensemanager.core.foundation.toLocalDateIn
import com.naveenapps.expensemanager.core.foundation.toMonthAndYearKey
import com.naveenapps.expensemanager.core.foundation.toYearKey
import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.model.BudgetTitle
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * The budget's own name when it has one, otherwise an automatic one from its period: "This
 * month's budget" / "This year's budget" for the current period, else the month or year.
 */
fun budgetTitle(
    name: String?,
    selectedMonth: String,
    periodType: BudgetPeriod = BudgetPeriod.MONTHLY,
    now: Instant = Clock.System.now(),
): BudgetTitle {
    name?.trim()?.takeIf { it.isNotEmpty() }?.let { return BudgetTitle.Custom(it) }
    val today = now.toLocalDateIn()
    return when (periodType) {
        BudgetPeriod.YEARLY -> if (selectedMonth == today.toYearKey()) {
            BudgetTitle.ThisYear
        } else {
            BudgetTitle.Period(selectedMonth)
        }

        BudgetPeriod.MONTHLY -> if (selectedMonth == today.toMonthAndYearKey()) {
            BudgetTitle.ThisMonth
        } else {
            selectedMonth.monthAndYearKeyToLocalDate()?.let { BudgetTitle.Month(it) } ?: BudgetTitle.Period(selectedMonth)
        }
    }
}

fun Budget.title(now: Instant = Clock.System.now()): BudgetTitle = budgetTitle(name, selectedMonth, periodType, now)
