package com.nkuppan.expensemanager.domain.model

import java.util.Date

data class Budget(
    val id: String,
    val name: String,
    val type: AccountType,
    val iconBackgroundColor: String,
    val iconName: String,
    val createdOn: Date,
    val updatedOn: Date,
    val amount: Double = 0.0,
    val creditLimit: Double = 0.0
)