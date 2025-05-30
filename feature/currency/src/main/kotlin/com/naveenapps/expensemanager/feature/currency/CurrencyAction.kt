package com.naveenapps.expensemanager.feature.currency

import com.naveenapps.expensemanager.core.model.Country
import com.naveenapps.expensemanager.core.model.TextFormat
import com.naveenapps.expensemanager.core.model.TextPosition

sealed class CurrencyAction {

    data object ClosePage : CurrencyAction()

    data object OpenCurrencySelection : CurrencyAction()

    data object DismissCurrencySelection : CurrencyAction()

    data class ChangeCurrencyNumberFormat(val textFormat: TextFormat) : CurrencyAction()

    data class ChangeCurrencyType(val textPosition: TextPosition) : CurrencyAction()

    data class SelectCurrency(val country: Country) : CurrencyAction()
}