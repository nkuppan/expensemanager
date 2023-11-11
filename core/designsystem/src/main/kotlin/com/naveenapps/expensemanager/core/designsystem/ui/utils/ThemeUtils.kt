package com.naveenapps.expensemanager.core.designsystem.ui.utils

import android.graphics.Color
import androidx.annotation.ColorInt


@ColorInt
fun getColorValue(colorValue: String?): Int {
    return runCatching {
        return Color.parseColor(if (colorValue?.isNotEmpty() == true) colorValue else "#000000")
    }.getOrNull() ?: Color.BLACK
}