package com.naveenapps.expensemanager.feature.settings.exchangemethod

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.model.ExchangeMethod
import com.naveenapps.expensemanager.core.model.StaticExchangeRate

@Stable
data class ExchangeMethodState(
    val method: ExchangeMethod = ExchangeMethod.DISABLED,
    /** Name of the configured API provider, or null when none is set up yet. */
    val apiProvider: String? = null,
    val staticRates: List<StaticExchangeRate> = emptyList(),
    /** Currencies the app actually uses (accounts and the display currency), for the editor. */
    val currencyCodes: List<String> = emptyList(),
    /** The fixed-rate record being added or edited, or null when the editor is closed. */
    val editor: StaticRateEditorState? = null,
)

@Stable
data class StaticRateEditorState(
    /** The record being edited; null when adding a new pair. */
    val original: StaticExchangeRate? = null,
    val baseCode: String = "",
    val quoteCode: String = "",
    val buyText: String = "",
    val sellText: String = "",
    val errorMessage: String? = null,
)
