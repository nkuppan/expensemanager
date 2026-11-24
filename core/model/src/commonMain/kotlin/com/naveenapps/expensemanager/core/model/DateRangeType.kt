package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable

@Stable
enum class DateRangeType {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    THIS_YEAR,
    ALL,
    CUSTOM,
}

fun DateRangeType.isCustom(): Boolean = this == DateRangeType.CUSTOM
