package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable

@Stable
data class Currency(
    val symbol: String,
    val name: String,
    val position: CurrencyPosition = CurrencyPosition.PREFIX,
    val namePlural: String = "",
    val nativeSymbol: String = "",
    val code: String = "",
)

fun CurrencyPosition.isPrefix(): Boolean {
    return this == CurrencyPosition.PREFIX
}

enum class CurrencyPosition {
    PREFIX,
    SUFFIX,
}

fun Currency.toDisplayValue(): String {
    return if (code.isNotBlank()) {
        "$code(${symbol})"
    } else {
        "(${symbol})"
    }
}
