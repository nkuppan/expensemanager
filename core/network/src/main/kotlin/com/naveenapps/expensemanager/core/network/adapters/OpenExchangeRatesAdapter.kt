package com.naveenapps.expensemanager.core.network.adapters

import com.naveenapps.expensemanager.core.model.CurrencyApiProfile
import com.naveenapps.expensemanager.core.network.ExchangeRateProviderAdapter
import kotlinx.serialization.json.JsonObject

internal object OpenExchangeRatesAdapter : ExchangeRateProviderAdapter {

    override fun buildUrl(profile: CurrencyApiProfile, baseCurrencyCode: String): String {
        val base = profile.baseUrl.trimEnd('/')
        return "$base/latest.json?base=$baseCurrencyCode"
    }

    override fun buildQuery(profile: CurrencyApiProfile): Map<String, String> {
        if (profile.apiKey.isBlank()) return emptyMap()
        return mapOf("app_id" to profile.apiKey)
    }

    override fun parseRates(
        body: JsonObject,
        baseCurrencyCode: String,
    ): Map<String, Double> = parseGenericRatesObject(body)
}
