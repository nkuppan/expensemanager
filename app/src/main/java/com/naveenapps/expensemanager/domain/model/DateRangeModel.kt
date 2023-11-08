package com.naveenapps.expensemanager.domain.model

data class DateRangeModel(
    val name: String,
    val description: String,
    val type: DateRangeType,
    val dateRanges: List<Long>,
)