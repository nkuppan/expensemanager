package com.naveenapps.expensemanager.core.data.utils

import android.content.Context
import com.naveenapps.expensemanager.core.common.utils.getCompactNumber
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFormat
import com.naveenapps.expensemanager.core.model.TextPosition
import java.text.NumberFormat

fun getCurrency(
    currency: Currency,
    amount: Double
): String {
    val reduceDigitFormat = "%.1f"
    val currencyFormatted = when (currency.format) {
        TextFormat.NONE -> reduceDigitFormat.format(amount)
        TextFormat.NUMBER_FORMAT -> {
            NumberFormat.getNumberInstance().format(reduceDigitFormat.format(amount).toDouble())
        }
    }

    return when (currency.position) {
        TextPosition.PREFIX -> {
            String.format(
                "%s%s",
                currency.symbol,
                currencyFormatted
            )
        }

        TextPosition.SUFFIX -> {
            String.format(
                "%s%s",
                currencyFormatted,
                currency.symbol
            )
        }
    }
}

fun getCompactCurrency(
    context: Context,
    currency: Currency,
    amount: Double
): String {
    val reduceDigitFormat = "%.1f"
    val currencyFormatted = getCompactNumber(
        when (currency.format) {
            TextFormat.NONE -> reduceDigitFormat.format(amount)
            TextFormat.NUMBER_FORMAT -> {
                NumberFormat.getNumberInstance().format(reduceDigitFormat.format(amount).toDouble())
            }
        }.toDouble()
    )

    return when (currency.position) {
        TextPosition.PREFIX -> {
            String.format(
                "%s%s",
                currency.symbol,
                currencyFormatted
            )
        }

        TextPosition.SUFFIX -> {
            String.format(
                "%s%s",
                currencyFormatted,
                currency.symbol
            )
        }
    }
}