package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable
import java.util.Date

@Stable
data class Category(
    val id: String,
    val name: String,
    val type: CategoryType,
    val storedIcon: StoredIcon,
    val createdOn: Date,
    val updatedOn: Date,
)
