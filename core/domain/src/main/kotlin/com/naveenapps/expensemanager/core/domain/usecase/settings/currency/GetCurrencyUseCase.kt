package com.naveenapps.expensemanager.core.domain.usecase.settings.currency

import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.repository.CountryRepository
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The app's display currency. Its settings store a copy of the symbol taken when it was picked;
 * the symbol shown is always the country list's current one for that ISO code, so a changed
 * symbol (e.g. SY£ → ليرة) applies to existing installs too, and totals and account rows
 * (which format by code) always agree.
 */
class GetCurrencyUseCase(
    private val repository: CurrencyRepository,
    private val countryRepository: CountryRepository? = null,
) {
    private val lock = Mutex()
    private var symbolsByCode: Map<String, String>? = null

    operator fun invoke(): Flow<Currency> = repository.getSelectedCurrency().map { currency ->
        val symbol = currency.code.takeIf { it.isNotBlank() }?.let { symbolFor(it) }
        if (symbol != null && symbol != currency.symbol) currency.copy(symbol = symbol) else currency
    }

    private suspend fun symbolFor(code: String): String? {
        val countries = countryRepository ?: return null
        val symbols = lock.withLock {
            symbolsByCode ?: countries.readCountries()
                .map { it.currency }
                .filter { it.code.isNotBlank() && it.symbol.isNotBlank() }
                .associate { it.code to it.symbol }
                .also { symbolsByCode = it }
        }
        return symbols[code]
    }
}
