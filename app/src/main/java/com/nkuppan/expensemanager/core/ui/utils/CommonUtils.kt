package com.nkuppan.expensemanager.core.ui.utils

import com.nkuppan.expensemanager.R

fun getCurrency(
    currencySymbol: Int,
    amount: Double
) = UiText.StringResource(
    R.string.amount_string,
    UiText.StringResource(currencySymbol),
    amount
)

fun getBalanceCurrency(
    currencySymbol: Int,
    amount: Double
) = UiText.StringResource(
    R.string.balance_amount_string,
    getCurrency(currencySymbol, amount)
)