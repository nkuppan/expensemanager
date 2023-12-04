package com.naveenapps.expensemanager.core.model

import java.util.Date

data class Category(
    val id: String,
    val name: String,
    val type: CategoryType,
    val storedIcon: StoredIcon,
    val createdOn: Date,
    val updatedOn: Date,
)
