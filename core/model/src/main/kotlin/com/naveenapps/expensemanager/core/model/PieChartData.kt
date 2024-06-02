package com.naveenapps.expensemanager.core.model

data class PieChartData(
    val name: String,
    val value: Float,
    val color: String,
)

fun getDummyPieChartData(categoryName: String, percent: Float): PieChartData {
    return PieChartData(
        name = categoryName,
        value = percent,
        color = "#40121212",
    )
}
