package com.nkuppan.expensemanager.core.ui.utils

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat


@ColorInt
fun getColorValue(colorValue: String?): Int {
    return Color.parseColor(if (colorValue?.isNotEmpty() == true) colorValue else "#000000")
}

@ColorInt
fun getThemeBackground(aContext: Context): Int {

    val typedValue = TypedValue()

    aContext.theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)

    return if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
        typedValue.data
    } else {
        ContextCompat.getColor(aContext, android.R.color.transparent)
    }
}