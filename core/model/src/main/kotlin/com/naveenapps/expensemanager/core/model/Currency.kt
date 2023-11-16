package com.naveenapps.expensemanager.core.model

data class Currency(
    val type: Int,
    val name: Int,
    val icon: Int,
    val position: CurrencySymbolPosition = CurrencySymbolPosition.PREFIX
)

enum class CurrencySymbolPosition {
    PREFIX,
    SUFFIX;
}


fun CurrencySymbolPosition.isPrefix(): Boolean {
    return this == CurrencySymbolPosition.PREFIX
}