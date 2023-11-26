package com.naveenapps.expensemanager.core.model

data class Currency(
    val symbol: Int,
    val name: Int,
    val position: TextPosition = TextPosition.PREFIX
)

enum class TextPosition {
    PREFIX,
    SUFFIX;
}


fun TextPosition.isPrefix(): Boolean {
    return this == TextPosition.PREFIX
}