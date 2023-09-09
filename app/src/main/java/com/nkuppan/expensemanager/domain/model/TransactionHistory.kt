package com.nkuppan.expensemanager.domain.model

data class TransactionHistory(
    val transaction: List<Transaction>,
    val monthTime: Long,
    val monthText: String,
)