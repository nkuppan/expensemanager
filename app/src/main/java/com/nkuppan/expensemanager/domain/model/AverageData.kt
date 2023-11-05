package com.nkuppan.expensemanager.domain.model

import com.nkuppan.expensemanager.ui.utils.UiText

data class WholeAverageData(
    val expenseAverageData: AverageData,
    val incomeAverageData: AverageData,
)

data class AverageData(
    val perDay: UiText,
    val perWeek: UiText,
    val perMonth: UiText,
)