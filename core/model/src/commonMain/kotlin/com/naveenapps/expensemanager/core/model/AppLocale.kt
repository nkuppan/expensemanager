package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable

@Stable
data class AppLocale(
    val tag: String,
    val titleResId: Int,
)
