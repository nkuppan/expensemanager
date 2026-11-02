package com.naveenapps.expensemanager.core.network.adapters

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.CurrencyApiPreset
import com.naveenapps.expensemanager.core.model.CurrencyApiProfile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertThrows
import org.junit.Test

class BuySellQuotesAdapterTest {

    // Buy/sell quote feed shape; values are old-lira per 1 unit.
    private val snapshot: JsonObject = Json.parseToJsonElement(
        """
        {
          "ok": true,
          "data": {
            "currencies": {
              "USD:region-a": {"buy": 13750, "sell": 13825, "change": 0.55},
              "EUR:region-a": {"buy": 15540, "sell": 15750, "change": 0.65},
              "USD:region-b": {"buy": 99999, "sell": 99999, "change": 0},
              "TRY:region-a": {"buy": 0, "sell": 282, "change": 0.36}
            }
          }
        }
        """.trimIndent(),
    ).jsonObject

    @Test
    fun buildUrl_usesUserEnteredUrlAsIs() {
        val profile = CurrencyApiProfile(
            preset = CurrencyApiPreset.BUY_SELL_QUOTES,
            baseUrl = " https://rates.example.com/feed ",
        )

        assertThat(BuySellQuotesAdapter.buildUrl(profile, "USD"))
            .isEqualTo("https://rates.example.com/feed")
    }

    @Test
    fun usdBase_returnsFirstRegionMidInNewLira() {
        val rates = BuySellQuotesAdapter.parseRates(snapshot, "USD")

        // (13750 + 13825) / 2 / 100
        assertThat(rates["SYP"]).isWithin(1e-9).of(137.875)
    }

    @Test
    fun sypBase_returnsInverseOfMid() {
        val rates = BuySellQuotesAdapter.parseRates(snapshot, "SYP")

        assertThat(rates["USD"]).isWithin(1e-12).of(1 / 137.875)
        assertThat(rates["EUR"]).isWithin(1e-12).of(1 / 156.45)
    }

    @Test
    fun foreignBase_derivesCrossRateThroughSyp() {
        val rates = BuySellQuotesAdapter.parseRates(snapshot, "EUR")

        assertThat(rates["USD"]).isWithin(1e-9).of(156.45 / 137.875)
        assertThat(rates["SYP"]).isWithin(1e-9).of(156.45)
        assertThat(rates).doesNotContainKey("EUR")
    }

    @Test
    fun ignoresLaterRegionsAndInvalidQuotes() {
        val rates = BuySellQuotesAdapter.parseRates(snapshot, "SYP")

        assertThat(rates.keys).containsExactly("USD", "EUR")
    }

    @Test
    fun unquotedBase_throwsSoCallerFallsBackToCache() {
        assertThrows(IllegalStateException::class.java) {
            BuySellQuotesAdapter.parseRates(snapshot, "JPY")
        }
    }

    @Test
    fun malformedBody_throws() {
        val body = Json.parseToJsonElement("""{"ok": false}""").jsonObject

        assertThrows(IllegalStateException::class.java) {
            BuySellQuotesAdapter.parseRates(body, "USD")
        }
    }
}
