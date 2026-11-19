package com.naveenapps.expensemanager.core.network

import com.naveenapps.expensemanager.core.model.CurrencyApiPreset
import com.naveenapps.expensemanager.core.network.adapters.BuySellQuotesAdapter
import com.naveenapps.expensemanager.core.network.adapters.CustomAdapter
import com.naveenapps.expensemanager.core.network.adapters.ExchangeRateHostAdapter
import com.naveenapps.expensemanager.core.network.adapters.FrankfurterAdapter
import com.naveenapps.expensemanager.core.network.adapters.OpenExchangeRatesAdapter

/** Registry mapping each [CurrencyApiPreset] to the adapter that knows its request/response shape. */
internal object ExchangeRateProviderAdapters {

    fun forPreset(preset: CurrencyApiPreset): ExchangeRateProviderAdapter = when (preset) {
        CurrencyApiPreset.FRANKFURTER -> FrankfurterAdapter
        CurrencyApiPreset.EXCHANGERATE_HOST -> ExchangeRateHostAdapter
        CurrencyApiPreset.OPEN_EXCHANGE_RATES -> OpenExchangeRatesAdapter
        CurrencyApiPreset.BUY_SELL_QUOTES -> BuySellQuotesAdapter
        CurrencyApiPreset.CUSTOM -> CustomAdapter
    }
}
