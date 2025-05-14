package com.naveenapps.expensemanager.feature.currency

import com.naveenapps.expensemanager.core.model.Currency

data class CurrencyState(
    val showCurrencySelection: Boolean,
    val currency: Currency
)