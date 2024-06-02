package com.naveenapps.expensemanager.core.data.utils

import android.content.Context
import com.naveenapps.expensemanager.core.common.utils.toDoubleOrNullWithLocale
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFormat
import com.naveenapps.expensemanager.core.model.TextPosition
import java.text.NumberFormat
import java.util.Locale

fun getCurrency(
    currency: Currency,
    amount: Double,
    locale: Locale = Locale.getDefault(),
): String {
    val reduceDigitFormat = "%.1f"
    val currencyFormatted = when (currency.format) {
        TextFormat.NONE -> reduceDigitFormat.format(locale, amount)
        TextFormat.NUMBER_FORMAT -> {
            NumberFormat.getNumberInstance(
                locale,
            ).format(
                reduceDigitFormat.format(
                    locale,
                    amount,
                ).toDoubleOrNullWithLocale(),
            )
        }
    }

    return when (currency.position) {
        TextPosition.PREFIX -> {
            String.format(
                locale,
                "%s%s",
                currency.symbol,
                currencyFormatted,
            )
        }

        TextPosition.SUFFIX -> {
            String.format(
                locale,
                "%s%s",
                currencyFormatted,
                currency.symbol,
            )
        }
    }
}


fun Context.convertFileToString(fileName: String): String? {
    return kotlin.runCatching {
        return this.assets.open(fileName).reader().readText()
    }.getOrNull()
}