package com.naveenapps.expensemanager.domain.model

data class WholeAverageData(
    val expenseAverageData: AverageData,
    val incomeAverageData: AverageData,
)

data class AverageData(
    val perDay: String,
    val perWeek: String,
    val perMonth: String,
)