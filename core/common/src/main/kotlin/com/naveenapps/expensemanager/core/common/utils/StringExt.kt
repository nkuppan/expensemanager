package com.naveenapps.expensemanager.core.common.utils

import java.util.Locale

fun Float.toPercentString(): String {
    return String.format("%.2f %%", this)
}

fun String.toCapitalize() = this.replaceFirstChar { char ->
    if (char.isLowerCase()) {
        char.titlecase(Locale.getDefault())
    } else {
        char.toString()
    }
}

fun Enum<*>.toCapitalize() = this.toString().lowercase().toCapitalize()
