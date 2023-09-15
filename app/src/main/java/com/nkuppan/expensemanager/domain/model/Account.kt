package com.nkuppan.expensemanager.domain.model

import java.io.Serializable
import java.util.Date

data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val iconBackgroundColor: String,
    val iconName: String,
    val createdOn: Date,
    val updatedOn: Date,
    val amount: Double = 0.0
) : Serializable