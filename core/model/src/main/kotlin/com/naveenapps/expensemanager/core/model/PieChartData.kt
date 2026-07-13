package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable

@Stable
data class PieChartData(
    val name: String,
    val value: Float,
    val color: String,
    val titleResId: Int? = null,
)

fun getDummyPieChartData(categoryName: String, percent: Float, titleResId: Int? = null): PieChartData {
    return PieChartData(
        name = categoryName,
        value = percent,
        color = "#40121212",
        titleResId = titleResId,
    )
}
