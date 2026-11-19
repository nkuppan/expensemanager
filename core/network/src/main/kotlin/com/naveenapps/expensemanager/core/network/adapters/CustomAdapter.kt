package com.naveenapps.expensemanager.core.network.adapters

import com.naveenapps.expensemanager.core.model.CurrencyApiProfile
import com.naveenapps.expensemanager.core.network.ExchangeRateProviderAdapter
import kotlinx.serialization.json.JsonObject

/**
 * Fallback for any provider without a dedicated adapter. Expects the generic
 * `{ "rates": { "CODE": number } }` shape; a user pointing this at a provider with a different
 * response shape needs a dedicated adapter registered in [com.naveenapps.expensemanager.core.network.ExchangeRateProviderAdapters]
 * instead.
 */
internal object CustomAdapter : ExchangeRateProviderAdapter {

    override fun buildUrl(profile: CurrencyApiProfile, baseCurrencyCode: String): String {
        val base = profile.baseUrl.trimEnd('/')
        return "$base?base=$baseCurrencyCode"
    }

    override fun buildHeaders(profile: CurrencyApiProfile): Map<String, String> {
        if (profile.apiKey.isBlank()) return emptyMap()
        return mapOf("Authorization" to "Bearer ${profile.apiKey}")
    }

    override fun parseRates(
        body: JsonObject,
        baseCurrencyCode: String,
    ): Map<String, Double> = parseGenericRatesObject(body)
}
