package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable

@Stable
data class WholeAverageData(
    val expenseAverageData: AverageData,
    val incomeAverageData: AverageData,
)

@Stable
data class AverageData(
    val perDay: String,
    val perWeek: String,
    val perMonth: String,
)
