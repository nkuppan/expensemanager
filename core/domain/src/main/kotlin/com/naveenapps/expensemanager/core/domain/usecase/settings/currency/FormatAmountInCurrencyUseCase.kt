package com.naveenapps.expensemanager.core.domain.usecase.settings.currency

import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.repository.CountryRepository
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Formats an amount in the currency it is actually stored in (an account's or transaction's own
 * ISO code), never relabelled with the app's display currency. Symbol comes from the country
 * list; placement follows the user's currency-position setting.
 */
class FormatAmountInCurrencyUseCase(
    private val countryRepository: CountryRepository,
    private val currencyRepository: CurrencyRepository,
) {
    private val lock = Mutex()
    private var currenciesByCode: Map<String, Currency>? = null

    suspend operator fun invoke(amount: Double, currencyCode: String): Amount = currencyRepository.getFormattedCurrency(
        Amount(amount = amount, currency = currencyFor(currencyCode)),
    )

    suspend fun currencyFor(currencyCode: String): Currency {
        val known = lock.withLock {
            currenciesByCode ?: countryRepository.readCountries()
                .map { it.currency }
                .filter { it.code.isNotBlank() }
                .associateBy { it.code }
                .also { currenciesByCode = it }
        }
        val position = currencyRepository.getSelectedCurrency().first().position
        // An unknown code still shows its ISO code rather than borrowing another symbol.
        val currency = known[currencyCode]
            ?: Currency(symbol = currencyCode, name = currencyCode, code = currencyCode)
        return currency.copy(position = position)
    }
}
