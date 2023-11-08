package com.naveenapps.expensemanager.domain.model

import java.util.Date

data class Category(
    val id: String,
    val name: String,
    val type: CategoryType,
    val iconBackgroundColor: String,
    val iconName: String,
    val createdOn: Date,
    val updatedOn: Date
)