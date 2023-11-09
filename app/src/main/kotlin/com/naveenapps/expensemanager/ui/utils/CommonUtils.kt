package com.naveenapps.expensemanager.ui.utils

import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.CurrencySymbolPosition

fun getCurrency(
    currency: Currency,
    amount: Double
): UiText {
    return when (currency.position) {
        CurrencySymbolPosition.PREFIX -> {
            UiText.StringResource(
                R.string.prefix_amount_string,
                UiText.StringResource(currency.type),
                amount
            )
        }

        CurrencySymbolPosition.SUFFIX -> {
            UiText.StringResource(
                R.string.suffix_amount_string,
                amount,
                UiText.StringResource(currency.type)
            )
        }
    }
}