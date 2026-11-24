package com.naveenapps.expensemanager.core.domain.usecase.settings.currency

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.Country
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.repository.RegionGuess
import org.junit.Test

class MatchCountryTest {

    private val countries = listOf(
        Country("India", "IN", "INR", Currency(symbol = "₹", name = "Indian Rupee", code = "INR")),
        Country("United States", "US", "USD", Currency(symbol = "$", name = "US Dollar", code = "USD")),
        Country("Nowhere", "XX", "", Currency(symbol = "", name = "", code = "")),
    )

    @Test
    fun `the SIM's country wins over the phone's region`() {
        val match = matchCountry(countries, listOf(RegionGuess("IN", "sim"), RegionGuess("US", "locale")))

        assertThat(match?.country?.currency?.code).isEqualTo("INR")
        assertThat(match?.source).isEqualTo("sim")
    }

    @Test
    fun `falls back to the next guess when one is unknown or has no usable currency`() {
        val guesses = listOf(RegionGuess("ZZ", "sim"), RegionGuess("XX", "network"), RegionGuess("us", "locale"))
        val match = matchCountry(countries, guesses)

        assertThat(match?.country?.countryCode).isEqualTo("US")
        assertThat(match?.source).isEqualTo("locale")
    }

    @Test
    fun `no match leaves the currency alone`() {
        assertThat(matchCountry(countries, listOf(RegionGuess("ZZ", "locale")))).isNull()
        assertThat(matchCountry(countries, emptyList())).isNull()
    }
}
