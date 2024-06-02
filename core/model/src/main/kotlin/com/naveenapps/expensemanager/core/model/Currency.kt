package com.naveenapps.expensemanager.core.model

data class Currency(
    val symbol: String,
    val name: String,
    val position: TextPosition = TextPosition.PREFIX,
    val format: TextFormat = TextFormat.NONE,
    val namePlural: String = "",
    val nativeSymbol: String = "",
)

enum class TextPosition {
    PREFIX,
    SUFFIX,
}

enum class TextFormat {
    NONE,
    NUMBER_FORMAT,
}

fun TextPosition.isPrefix(): Boolean {
    return this == TextPosition.PREFIX
}
