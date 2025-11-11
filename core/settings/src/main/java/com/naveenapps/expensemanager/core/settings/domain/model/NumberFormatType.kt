package com.naveenapps.expensemanager.core.settings.domain.model

enum class NumberFormatType {
    WITHOUT_ANY_SEPARATOR,
    WITH_COMMA_SEPARATOR,
}


inline fun <reified T> toEnumValue(id: Int): T where T : Enum<T> {
    return enumValues<T>().find { it.ordinal == id }
        ?: enumValues<T>().first()
}