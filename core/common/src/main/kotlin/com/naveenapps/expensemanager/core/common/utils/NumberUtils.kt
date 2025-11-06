package com.naveenapps.expensemanager.core.common.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

private val SUFFIX_VALUES = charArrayOf(' ', 'k', 'M', 'B', 'T', 'P', 'E')
private val SINGLE_DECIMAL_NUMBER_FORMAT = DecimalFormat("#0.0")
private val THREE_DECIMAL_NUMBER_FORMAT = DecimalFormat("#,##0")
private val NUMBER_FORMAT by lazy {
    NumberFormat.getInstance(Locale.getDefault())
}

fun getCompactNumber(number: Number): String {
    val numValue = number.toLong()
    val value = floor(log10(numValue.toDouble())).toInt()
    val base = value / 3
    return if (value >= 3 && base < SUFFIX_VALUES.size) {
        SINGLE_DECIMAL_NUMBER_FORMAT.format(
            numValue / 10.0.pow((base * 3).toDouble()),
        ) + SUFFIX_VALUES[base]
    } else {
        THREE_DECIMAL_NUMBER_FORMAT.format(numValue)
    }
}

fun String?.toDoubleOrNullWithLocale(): Double? {
    this ?: return null
    return try {
        val number: Number? = NUMBER_FORMAT.parse(this)
        number?.toDouble() ?: 0.0
    } catch (e: Exception) {
        null
    }
}

fun Double.toStringWithLocale(): String {
    return try {
        NUMBER_FORMAT.format(this)
    } catch (e: Exception) {
        NUMBER_FORMAT.format(0.0)
    }
}