package com.naveenapps.expensemanager.feature.currency

import com.naveenapps.expensemanager.core.model.Country
import com.naveenapps.expensemanager.core.model.TextPosition
import com.naveenapps.expensemanager.core.settings.domain.model.NumberFormatType

sealed class CurrencyAction {

    data object ClosePage : CurrencyAction()

    data object OpenCurrencySelection : CurrencyAction()

    data object DismissCurrencySelection : CurrencyAction()

    data class ChangeCurrencyNumberFormat(val numberFormatType: NumberFormatType) : CurrencyAction()

    data class ChangeCurrencyType(val textPosition: TextPosition) : CurrencyAction()

    data class SelectCurrency(val country: Country) : CurrencyAction()
}