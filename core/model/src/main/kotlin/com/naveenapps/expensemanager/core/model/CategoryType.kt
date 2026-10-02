package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable

@Stable
enum class CategoryType {
    INCOME,
    EXPENSE,
}

fun CategoryType.isIncome(): Boolean = this == CategoryType.INCOME

fun CategoryType.isExpense(): Boolean = this == CategoryType.EXPENSE
