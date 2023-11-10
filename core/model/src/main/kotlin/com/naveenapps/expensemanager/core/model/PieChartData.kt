package com.naveenapps.expensemanager.core.model

import android.graphics.Color
import androidx.annotation.ColorInt

data class PieChartData(
    var name: String,
    var value: Float,
    @ColorInt var color: Int,
)

fun getDummyPieChartData(categoryName: String, percent: Float): PieChartData {
    return PieChartData(
        name = categoryName,
        value = percent,
        color = Color.parseColor("#40121212"),
    )
}