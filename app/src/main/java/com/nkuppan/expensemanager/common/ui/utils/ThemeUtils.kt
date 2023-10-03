package com.nkuppan.expensemanager.common.ui.utils

import android.graphics.Color
import androidx.annotation.ColorInt


@ColorInt
fun getColorValue(colorValue: String?): Int {
    return runCatching {
        return Color.parseColor(if (colorValue?.isNotEmpty() == true) colorValue else "#000000")
    }.getOrNull() ?: Color.BLACK
}