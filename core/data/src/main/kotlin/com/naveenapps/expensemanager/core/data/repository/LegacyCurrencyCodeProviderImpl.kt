package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.database.LegacyCurrencyCodeProvider
import com.naveenapps.expensemanager.core.repository.CountryRepository
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * The single currency every pre-multi-currency row was in, for `Migration8To10`: the one the
 * user confirmed for a backup being restored, otherwise the app's selected currency.
 *
 * The saved `code` can't be trusted blindly: the currency settings screen used to update only
 * the name/symbol and keep a stale code. So it's re-derived from the saved name + symbol against
 * the country list (a unique match wins, and the saved setting is repaired to it), falling back
 * to the saved code, which is never blank (the built-in default carries one).
 *
 * Blocking is acceptable: Room runs migrations on the thread that first opens the database,
 * which is never the main thread.
 */
class LegacyCurrencyCodeProviderImpl(
    private val currencyRepository: CurrencyRepository,
    private val countryRepository: CountryRepository,
) : LegacyCurrencyCodeProvider {

    override fun legacyCurrencyCode(): String = runBlocking {
        // Restoring an old backup: the user said which currency it's in; that wins, since the
        // backup may come from another device or a different currency than the one set here.
        currencyRepository.getRestoreCurrencyCode()?.takeIf { it.isNotBlank() }
            ?.let { return@runBlocking it }

        val selected = currencyRepository.getSelectedCurrency().first()
        val matchingCodes = countryRepository.readCountries()
            .map { it.currency }
            .filter { it.name == selected.name && it.symbol == selected.symbol }
            .map { it.code }
            .filter { it.isNotBlank() }
            .toSet()
        val code = matchingCodes.singleOrNull() ?: selected.code
        if (code != selected.code) {
            currencyRepository.saveCurrency(selected.copy(code = code))
        }
        check(code.isNotBlank()) { "Cannot determine the legacy currency code" }
        code
    }
}
