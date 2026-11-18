package com.naveenapps.expensemanager.core.domain.usecase.settings.currency

import com.naveenapps.expensemanager.core.model.Country
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.CountryRepository
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import com.naveenapps.expensemanager.core.repository.DevicePropertyRepository
import com.naveenapps.expensemanager.core.repository.RegionGuess
import kotlinx.coroutines.flow.first

/** What setup pre-selected: the country, and which signal found it ("sim", "network", "locale"). */
data class PreselectedCurrency(val country: Country, val source: String)

/**
 * On a fresh install, saves the currency of the user's country (₹ for India rather than the old
 * US-dollar default), so most people never need to change it during setup. Does nothing once a
 * currency has been saved, so it never overrides the user's own choice.
 */
class PreselectCountryCurrencyUseCase(
    private val currencyRepository: CurrencyRepository,
    private val countryRepository: CountryRepository,
    private val devicePropertyRepository: DevicePropertyRepository,
    private val saveCurrencyUseCase: SaveCurrencyUseCase,
) {
    /** The pre-selected currency, or null when one was already chosen or no country matched. */
    suspend operator fun invoke(): PreselectedCurrency? {
        if (currencyRepository.isCurrencyChosen().first()) return null
        val countries = countryRepository.readCountries()
        val match = matchCountry(countries, devicePropertyRepository.getRegionCountryCodes()) ?: return null
        return match.takeIf { saveCurrencyUseCase(it.country.currency) is Resource.Success }
    }
}

/** The first guess (most reliable first) that names a known country with a usable currency. */
internal fun matchCountry(countries: List<Country>, guesses: List<RegionGuess>): PreselectedCurrency? {
    val byCode = countries.associateBy { it.countryCode.uppercase() }
    return guesses.firstNotNullOfOrNull { guess ->
        byCode[guess.countryCode.uppercase()]
            ?.takeIf { it.currency.symbol.isNotBlank() && it.currency.name.isNotBlank() }
            ?.let { PreselectedCurrency(it, guess.source) }
    }
}
