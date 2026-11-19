package com.naveenapps.expensemanager.feature.settings.currencyapi

import com.naveenapps.expensemanager.core.model.CurrencyApiPreset

sealed class CurrencyApiProfileAction {

    data object ClosePage : CurrencyApiProfileAction()

    data object Save : CurrencyApiProfileAction()

    data class SelectPreset(val preset: CurrencyApiPreset) : CurrencyApiProfileAction()

    data class ChangeBaseUrl(val baseUrl: String) : CurrencyApiProfileAction()

    data class ChangeApiKey(val apiKey: String) : CurrencyApiProfileAction()
}
