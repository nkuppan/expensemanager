package com.naveenapps.expensemanager.core.model

data class TextFieldValue<T>(
    var value: T,
    var valueError: Boolean = false,
    val onValueChange: ((T) -> Unit)?,
)

data class FieldValue<T>(
    var value: T,
    var valueError: Boolean = false,
    val onValueChange: (T) -> Unit,
    val onClick: () -> Unit,
)
