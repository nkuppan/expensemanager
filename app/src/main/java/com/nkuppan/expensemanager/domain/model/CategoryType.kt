package com.nkuppan.expensemanager.domain.model

enum class CategoryType {
    INCOME,
    EXPENSE
}

fun CategoryType.isIncome(): Boolean {
    return this == CategoryType.INCOME
}

fun CategoryType.isExpense(): Boolean {
    return this == CategoryType.EXPENSE
}
