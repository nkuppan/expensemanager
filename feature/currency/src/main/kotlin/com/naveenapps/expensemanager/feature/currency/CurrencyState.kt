package com.naveenapps.expensemanager.feature.currency

import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.settings.domain.model.NumberFormatType

data class CurrencyState(
    val showCurrencySelection: Boolean,
    val numberFormatType: NumberFormatType,
    val currency: Currency
)