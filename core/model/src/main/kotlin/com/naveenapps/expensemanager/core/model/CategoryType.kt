package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable

@Stable
enum class CategoryType {
    INCOME,
    EXPENSE,
}

fun CategoryType.isIncome(): Boolean {
    return this == CategoryType.INCOME
}

fun CategoryType.isExpense(): Boolean {
    return this == CategoryType.EXPENSE
}
