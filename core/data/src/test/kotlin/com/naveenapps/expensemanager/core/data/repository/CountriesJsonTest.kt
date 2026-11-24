package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.data.dto.CountryResponseDto
import com.naveenapps.expensemanager.core.data.dto.CurrencyResponseDto
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The bundled countries.json decodes completely with kotlinx.serialization (it replaced Gson;
 * OpenSpec change make-core-repository-multiplatform, task 2.6). Every field of every country is
 * checked against the raw JSON tree, which doesn't use the typed mapping, so a renamed or dropped
 * field fails here.
 */
class CountriesJsonTest {

    private val text = File("src/main/assets/countries.json").readText()
    private val json = Json { ignoreUnknownKeys = true } // as provided by KoinAppModule

    private fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)?.content

    @Test
    fun everyCountryAndCurrencyMatchesTheFile() {
        val decoded = decodeCountries(json, text).counties
        val raw = Json.parseToJsonElement(text).jsonObject.getValue("countries").jsonArray

        assertEquals(246, decoded.size)
        assertEquals(raw.size, decoded.size)
        decoded.zip(raw).forEach { (country, element) -> assertCountry(element.jsonObject, country) }
    }

    @Test
    fun theCountryWithoutACurrencyHasNone() {
        val decoded = decodeCountries(json, text).counties
        val raw = Json.parseToJsonElement(text).jsonObject.getValue("countries").jsonArray
        val withoutCurrency = raw.indices.filter { "currency" !in raw[it].jsonObject }

        assertEquals(1, withoutCurrency.size)
        assertNull(decoded[withoutCurrency.single()].currencyResponseDto)
    }

    @Test
    fun unknownKeysAreIgnoredLikeGsonDid() {
        val withExtra = """{"countries":[{"name":"Testland","code":"TL","new_field":42}],"version":2}"""
        val country = decodeCountries(json, withExtra).counties.single()
        assertEquals("Testland", country.name)
        assertEquals("TL", country.countryCode)
    }

    private fun assertCountry(raw: JsonObject, country: CountryResponseDto) {
        val where = raw.string("code")
        assertEquals(where, raw.string("name"), country.name)
        assertEquals(where, raw.string("code"), country.countryCode)
        assertEquals(where, raw.string("dial_code"), country.dialCode)
        assertEquals(where, raw.string("continent"), country.continent)
        assertEquals(where, raw.string("group"), country.countryGroup)
        assertEquals(where, raw.string("states"), country.countryStates)
        assertEquals(where, raw.string("currency_code"), country.currencyCode)
        val rawCurrency = raw["currency"]?.jsonObject
        if (rawCurrency == null) {
            assertNull(where, country.currencyResponseDto)
        } else {
            assertCurrency("$where currency", rawCurrency, country.currencyResponseDto!!)
        }
    }

    private fun assertCurrency(where: String, raw: JsonObject, currency: CurrencyResponseDto) {
        assertEquals(where, raw.string("symbol"), currency.symbol)
        assertEquals(where, raw.string("name"), currency.name)
        assertEquals(where, raw.string("symbol_native"), currency.nativeSymbol)
        assertEquals(where, raw.getValue("decimal_digits").jsonPrimitive.int, currency.decimalDigits)
        assertEquals(where, raw.getValue("rounding").jsonPrimitive.float, currency.rounding, 0f)
        assertEquals(where, raw.string("name_plural"), currency.namePlural)
        assertEquals(where, raw.string("code"), currency.code)
    }
}
