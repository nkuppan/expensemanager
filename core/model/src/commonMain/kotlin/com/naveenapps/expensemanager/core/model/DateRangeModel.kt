package com.naveenapps.expensemanager.core.model

data class DateRangeModel(
    val name: String,
    val description: String,
    val type: DateRangeType,
    val dateRanges: List<Long>,
)
