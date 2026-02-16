package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable

@Stable
data class TextFieldValue<T>(
    var value: T,
    var valueError: Boolean = false,
    val onValueChange: ((T) -> Unit)?,
)