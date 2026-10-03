package com.naveenapps.expensemanager.core.model

import java.time.DayOfWeek

/** Insights for the transactions matching the Analysis filter (period, accounts, categories). */
data class SpendingInsights(
    val totalExpense: Double,
    val totalIncome: Double,
    /** (income − expense) / income, or null when there was no income in the period. */
    val savingsRate: Float?,
    /** Expense categories, largest first. */
    val categories: List<CategorySpend>,
    /** Monday..Sunday, always 7 entries. */
    val weekdays: List<WeekdaySpend>,
    /** The weekday with the most spending, or null with no expenses. */
    val busiestWeekday: DayOfWeek?,
    /** Largest single expenses, largest first (full transactions, so the UI can open them). */
    val biggestExpenses: List<Transaction>,
)

data class CategorySpend(
    val category: Category,
    val total: Double,
    /** Fraction (0..1) of all expenses in the period. */
    val share: Float,
    val count: Int,
)

data class WeekdaySpend(
    val dayOfWeek: DayOfWeek,
    val total: Double,
)
