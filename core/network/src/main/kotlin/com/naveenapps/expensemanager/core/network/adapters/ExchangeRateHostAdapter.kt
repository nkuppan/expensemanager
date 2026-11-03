package com.naveenapps.expensemanager.core.network.adapters

import com.naveenapps.expensemanager.core.model.CurrencyApiProfile
import com.naveenapps.expensemanager.core.network.ExchangeRateProviderAdapter
import kotlinx.serialization.json.JsonObject

internal object ExchangeRateHostAdapter : ExchangeRateProviderAdapter {

    override fun buildUrl(profile: CurrencyApiProfile, baseCurrencyCode: String): String {
        val base = profile.baseUrl.trimEnd('/')
        return "$base/latest?base=$baseCurrencyCode"
    }

    override fun buildQuery(profile: CurrencyApiProfile): Map<String, String> {
        if (profile.apiKey.isBlank()) return emptyMap()
        return mapOf("access_key" to profile.apiKey)
    }

    override fun parseRates(
        body: JsonObject,
        baseCurrencyCode: String,
    ): Map<String, Double> = parseGenericRatesObject(body)
}
