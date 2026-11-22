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
    /**
     * Stable key of a built-in category seeded by the app ("food", "salary", ...), as stored in the
     * `default_category_key` column. When set, show the category with `displayName()` (Compose) or
     * `localizedCategoryName()` so it follows the app's language. Always null for user-created
     * categories, whose [name] is free text shown as typed. Never shown to the user itself.
     */
    val defaultKey: String? = null,
)
