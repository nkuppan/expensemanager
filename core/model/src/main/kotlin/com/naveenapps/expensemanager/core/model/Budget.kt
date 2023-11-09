package com.naveenapps.expensemanager.core.model

import java.util.Date

data class Budget(
    val id: String,
    val name: String,
    val iconBackgroundColor: String,
    val iconName: String,
    val amount: Double = 0.0,
    val selectedMonth: String,
    val categories: List<String>,
    val accounts: List<String>,
    val isAllAccountsSelected: Boolean,
    val isAllCategoriesSelected: Boolean,
    val createdOn: Date,
    val updatedOn: Date
)