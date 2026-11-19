package com.naveenapps.expensemanager.core.network.adapters

import com.naveenapps.expensemanager.core.model.CurrencyApiProfile
import com.naveenapps.expensemanager.core.network.ExchangeRateProviderAdapter
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Generic "buy/sell quotes" format for Syrian Pound market-rate feeds. No endpoint is bundled:
 * the user enters the full URL of a feed that returns
 * `{ "data": { "currencies": { "USD:region": { "buy": 13750, "sell": 13825 } } } }`.
 *
 * Every quote is "old lira per 1 unit of CODE". Keys may carry a `:region` suffix; when a code is
 * quoted for several regions, the first one listed wins. This adapter takes the buy/sell mid,
 * converts it to new lira (÷ [OLD_TO_NEW_LIRA]) since the app's SYP is the post-redenomination
 * new lira, and derives cross rates so any quoted currency — not just SYP — can be the base:
 * `rate(B -> T) = sypPerUnit(B) / sypPerUnit(T)`.
 */
internal object BuySellQuotesAdapter : ExchangeRateProviderAdapter {

    const val SYP = "SYP"
    private const val OLD_TO_NEW_LIRA = 100.0

    override fun buildUrl(profile: CurrencyApiProfile, baseCurrencyCode: String): String = profile.baseUrl.trim()

    override fun parseRates(
        body: JsonObject,
        baseCurrencyCode: String,
    ): Map<String, Double> {
        val sypPerUnit = parseSypPerUnit(body)
        val basePerSyp = sypPerUnit[baseCurrencyCode]
        checkNotNull(basePerSyp) { "Quote feed has no rate for $baseCurrencyCode" }
        return sypPerUnit
            .filterKeys { it != baseCurrencyCode }
            .mapValues { (_, targetPerSyp) -> basePerSyp / targetPerSyp }
    }

    /** New-lira price of one unit of each quoted currency, plus SYP itself at 1.0. */
    private fun parseSypPerUnit(body: JsonObject): Map<String, Double> {
        val currencies = body["data"]?.jsonObject?.get("currencies")?.jsonObject
            ?: return emptyMap()
        val rates = LinkedHashMap<String, Double>()
        currencies.forEach { (key, value) ->
            val code = key.substringBefore(':')
            if (code in rates) return@forEach
            val quote = value.jsonObject
            val buy = quote["buy"]?.jsonPrimitive?.doubleOrNull ?: return@forEach
            val sell = quote["sell"]?.jsonPrimitive?.doubleOrNull ?: return@forEach
            if (buy <= 0.0 || sell <= 0.0) return@forEach
            rates[code] = (buy + sell) / 2 / OLD_TO_NEW_LIRA
        }
        return if (rates.isEmpty()) rates else rates + (SYP to 1.0)
    }
}
