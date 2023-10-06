package com.nkuppan.expensemanager.domain.model

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
}

fun TransactionType.isTransfer(): Boolean {
    return this == TransactionType.TRANSFER
}