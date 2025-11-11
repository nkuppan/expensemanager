package com.naveenapps.expensemanager.core.model

data class Currency(
    val symbol: String,
    val name: String,
    val position: TextPosition = TextPosition.PREFIX,
    val namePlural: String = "",
    val nativeSymbol: String = "",
)

enum class TextPosition {
    PREFIX,
    SUFFIX,
}

fun TextPosition.isPrefix(): Boolean {
    return this == TextPosition.PREFIX
}
