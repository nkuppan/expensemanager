package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable

@Stable
enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER,
    REFUND,
}

fun TransactionType.isTransfer(): Boolean = this == TransactionType.TRANSFER

fun TransactionType.isIncome(): Boolean = this == TransactionType.INCOME

fun TransactionType.isExpense(): Boolean = this == TransactionType.EXPENSE

fun TransactionType.isRefund(): Boolean = this == TransactionType.REFUND
