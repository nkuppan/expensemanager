package com.nkuppan.expensemanager.core.ui.utils

import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.Currency
import com.nkuppan.expensemanager.domain.model.CurrencySymbolPosition

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

fun getBalanceCurrency(
    currency: Currency,
    amount: Double
) = UiText.StringResource(
    R.string.balance_amount_string,
    getCurrency(currency, amount)
)