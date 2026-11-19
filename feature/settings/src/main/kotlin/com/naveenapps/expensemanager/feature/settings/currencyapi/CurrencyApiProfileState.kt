package com.naveenapps.expensemanager.feature.settings.currencyapi

import com.naveenapps.expensemanager.core.model.CurrencyApiPreset

data class CurrencyApiProfileState(
    val preset: CurrencyApiPreset = CurrencyApiPreset.FRANKFURTER,
    val baseUrl: String = CurrencyApiPreset.FRANKFURTER.defaultBaseUrl,
    val apiKey: String = "",
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
)
