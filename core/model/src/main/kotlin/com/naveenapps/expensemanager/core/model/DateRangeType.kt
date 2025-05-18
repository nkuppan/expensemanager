package com.naveenapps.expensemanager.core.model

enum class DateRangeType {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    THIS_YEAR,
    ALL,
    CUSTOM,
}

fun DateRangeType.isCustom(): Boolean {
    return this == DateRangeType.CUSTOM
}
