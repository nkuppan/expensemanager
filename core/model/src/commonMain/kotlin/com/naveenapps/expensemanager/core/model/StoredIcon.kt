package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable

@Stable
data class StoredIcon(
    val name: String,
    val backgroundColor: String,
    // Absolute path to a user-picked/captured photo, copied into app-private storage. When set,
    // UI should render this photo (plain, cropped to a circle) instead of [name]/[backgroundColor].
    val customImagePath: String? = null,
)
