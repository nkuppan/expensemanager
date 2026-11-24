package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable

@Stable
data class PieChartData(
    val name: String,
    val value: Float,
    val color: String,
    /** [Category.defaultKey] of the slice's category; see `displayName()`. */
    val categoryDefaultKey: String? = null,
)

fun getDummyPieChartData(categoryName: String, percent: Float, categoryDefaultKey: String? = null): PieChartData = PieChartData(
    name = categoryName,
    value = percent,
    color = "#40121212",
    categoryDefaultKey = categoryDefaultKey,
)
