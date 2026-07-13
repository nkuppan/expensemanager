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
     * Set only for the built-in categories seeded by the app. When non-null, UI code should
     * prefer `stringResource(titleResId)` over [name] so the category name follows the app's
     * selected language. Always null for user-created (or user-renamed) categories, whose
     * [name] is arbitrary free text and must be shown as-is.
     */
    val titleResId: Int? = null,
)
