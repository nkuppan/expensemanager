package com.nkuppan.expensemanager.data.repository

import com.nkuppan.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.nkuppan.expensemanager.core.model.Currency
import com.nkuppan.expensemanager.data.R
import com.nkuppan.expensemanager.data.datastore.CurrencyDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CurrencyRepositoryImpl(
    private val dataStore: CurrencyDataStore,
    private val dispatchers: AppCoroutineDispatchers
) : CurrencyRepository {

    private fun getDefaultCurrency(): Currency {
        return Currency(
            R.string.default_currency_type,
            R.string.default_currency_name
        )
    }

    override suspend fun saveCurrency(currency: Currency): Boolean = withContext(dispatchers.io) {
        dataStore.setCurrencySymbol(currency.type)
        true
    }

    override fun getSelectedCurrency(): Flow<Currency> {
        val defaultCurrency = getDefaultCurrency()
        val currencies = getAllCurrency()
        return dataStore.getCurrencySymbol(defaultCurrency.type).map { mode ->
            currencies.find { currency -> currency.type == mode } ?: defaultCurrency
        }
    }

    override fun getAllCurrency(): List<Currency> {
        return listOf(
            Currency(
                R.string.dollar_type,
                R.string.dollar_name
            ),
            Currency(
                R.string.pound_type,
                R.string.pound_name
            ),
            Currency(
                R.string.euro_type,
                R.string.euro_name
            ),
            Currency(
                R.string.yen_type,
                R.string.yen_name
            ),
            Currency(
                R.string.swiss_franc_type,
                R.string.swiss_franc_name
            ),
            Currency(
                R.string.swedish_krona_type,
                R.string.swedish_krona_name
            ),
            Currency(
                R.string.won_type,
                R.string.won_name
            ),
            Currency(
                R.string.rupee_type,
                R.string.rupee_name
            )
        )
    }
}