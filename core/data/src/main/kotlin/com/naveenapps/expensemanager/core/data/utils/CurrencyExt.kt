package com.naveenapps.expensemanager.core.data.utils

import android.content.Context
import com.naveenapps.expensemanager.core.common.utils.toDoubleOrNullWithLocale
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFormat
import com.naveenapps.expensemanager.core.model.TextPosition
import java.text.NumberFormat
import java.util.Locale


private val CustomNumberFormat by lazy {
    NumberFormat.getNumberInstance(Locale.getDefault())
}

fun getCurrency(
    currency: Currency,
    amount: Double,
): String {
    val reduceDigitFormat = "%.1f"
    val currencyFormatted = when (currency.format) {
        TextFormat.NONE -> reduceDigitFormat.format(amount)
        TextFormat.NUMBER_FORMAT -> {
            CustomNumberFormat.format(
                reduceDigitFormat.format(
                    amount,
                ).toDoubleOrNullWithLocale(),
            )
        }
    }

    return when (currency.position) {
        TextPosition.PREFIX -> {
            "${currency.symbol}${currencyFormatted}"
        }

        TextPosition.SUFFIX -> {
            "${currencyFormatted}${currency.symbol}"
        }
    }
}


fun Context.convertFileToString(fileName: String): String? {
    return kotlin.runCatching {
        return this.assets.open(fileName).reader().readText()
    }.getOrNull()
}