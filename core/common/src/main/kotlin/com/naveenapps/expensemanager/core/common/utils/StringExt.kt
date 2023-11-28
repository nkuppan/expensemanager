package com.naveenapps.expensemanager.core.common.utils

import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.floor
import kotlin.math.log10


fun Float.toPercentString(): String {
    return String.format("%.2f %%", this)
}

fun String.toCapitalize() = this.replaceFirstChar { char ->
    if (char.isLowerCase()) {
        char.titlecase(Locale.getDefault())
    } else {
        char.toString()
    }
}

fun Enum<*>.toCapitalize() = this.toString().lowercase().toCapitalize()


fun getCompactNumber(number: Number): String {
    val suffix = charArrayOf(' ', 'k', 'M', 'B', 'T', 'P', 'E')
    val numValue = number.toLong()
    val value = floor(log10(numValue.toDouble())).toInt()
    val base = value / 3
    return if (value >= 3 && base < suffix.size) {
        DecimalFormat("#0.0").format(
            numValue / Math.pow(
                10.0,
                (base * 3).toDouble()
            )
        ) + suffix[base]
    } else {
        DecimalFormat("#,##0").format(numValue)
    }
}