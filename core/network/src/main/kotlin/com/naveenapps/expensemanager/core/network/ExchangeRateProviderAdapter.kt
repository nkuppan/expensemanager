package com.naveenapps.expensemanager.core.network

import com.naveenapps.expensemanager.core.model.CurrencyApiProfile
import kotlinx.serialization.json.JsonObject

/**
 * One adapter per provider preset: each knows that provider's own URL shape, auth placement,
 * and response JSON structure, and normalizes all of it to a single `code -> rate` map. Adding a
 * provider means adding an adapter and registering it in [ExchangeRateProviderAdapters] — nothing
 * else in the network layer needs to change.
 */
internal interface ExchangeRateProviderAdapter {

    fun buildUrl(profile: CurrencyApiProfile, baseCurrencyCode: String): String

    fun buildHeaders(profile: CurrencyApiProfile): Map<String, String> = emptyMap()

    fun buildQuery(profile: CurrencyApiProfile): Map<String, String> = emptyMap()

    fun parseRates(body: JsonObject, baseCurrencyCode: String): Map<String, Double>
}
