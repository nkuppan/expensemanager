package com.nkuppan.expensemanager.core.model

data class TransactionHistory(
    val transaction: List<Transaction>,
    val monthTime: Long,
    val monthText: String,
)