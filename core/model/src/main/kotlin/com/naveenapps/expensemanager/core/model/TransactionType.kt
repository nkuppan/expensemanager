package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable

@Stable
enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER,
}

fun TransactionType.isTransfer(): Boolean {
    return this == TransactionType.TRANSFER
}

fun TransactionType.isIncome(): Boolean {
    return this == TransactionType.INCOME
}

fun TransactionType.isExpense(): Boolean {
    return this == TransactionType.EXPENSE
}
