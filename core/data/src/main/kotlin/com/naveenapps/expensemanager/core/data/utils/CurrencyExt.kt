package com.naveenapps.expensemanager.core.data.utils

import android.content.Context
import com.naveenapps.expensemanager.core.common.R
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.CurrencySymbolPosition

fun getCurrency(
    context: Context,
    currency: Currency,
    amount: Double
): String {
    return when (currency.position) {
        CurrencySymbolPosition.PREFIX -> {
            context.getString(
                R.string.prefix_amount_string,
                context.getString(currency.type),
                amount
            )
        }

        CurrencySymbolPosition.SUFFIX -> {
            context.getString(
                R.string.suffix_amount_string,
                amount,
                context.getString(currency.type)
            )
        }
    }
}