package com.naveenapps.expensemanager.core.model

import java.util.Date

data class Budget(
    val id: String,
    val amount: Double = 0.0,
    /**
     * Round-trip key identifying the period this budget covers. For [BudgetPeriod.MONTHLY]
     * budgets this is a month+year key (see `Date.toMonthAndYearKey`); for [BudgetPeriod.YEARLY]
     * budgets this is a year-only key (see `Date.toYear`). Which format applies is determined by
     * [periodType].
     */
    val selectedMonth: String,
    val periodType: BudgetPeriod = BudgetPeriod.MONTHLY,
    val categories: List<String>,
    val accounts: List<String>,
    val isAllAccountsSelected: Boolean,
    val isAllCategoriesSelected: Boolean,
    val createdOn: Date,
    val updatedOn: Date,
)
