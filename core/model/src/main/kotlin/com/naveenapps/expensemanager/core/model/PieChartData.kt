package com.naveenapps.expensemanager.core.model

data class PieChartData(
    var name: String,
    var value: Float,
    var color: String,
)

fun getDummyPieChartData(categoryName: String, percent: Float): PieChartData {
    return PieChartData(
        name = categoryName,
        value = percent,
        color = "#40121212",
    )
}