package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable

/**
 * User-supplied connection details for a currency exchange-rate API. The base URL and API key
 * are both plain user input; there is no in-app payment/subscription flow — the key is whatever
 * the user already generated on the provider's own platform.
 */
@Stable
data class CurrencyApiProfile(
    val preset: CurrencyApiPreset = CurrencyApiPreset.FRANKFURTER,
    val baseUrl: String = CurrencyApiPreset.FRANKFURTER.defaultBaseUrl,
    val apiKey: String = "",
)

/**
 * Response shape differs per provider, so each preset carries its own parsing rule.
 * CUSTOM expects the generic `{ "rates": { "CODE": number, ... } }` shape.
 */
enum class CurrencyApiPreset(val defaultBaseUrl: String) {
    FRANKFURTER("https://api.frankfurter.dev/v1"),
    EXCHANGERATE_HOST("https://api.exchangerate.host"),
    OPEN_EXCHANGE_RATES("https://openexchangerates.org/api"),
    BUY_SELL_QUOTES(""),
    CUSTOM(""),
}
