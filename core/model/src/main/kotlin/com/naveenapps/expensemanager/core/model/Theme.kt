package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable

@Stable
data class Theme(
    val mode: Int,
    val titleResId: Int,
)
