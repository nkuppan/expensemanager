package com.nkuppan.expensemanager.data.repository

import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.data.datastore.CurrencyDataStore
import com.nkuppan.expensemanager.domain.model.Currency
import com.nkuppan.expensemanager.domain.model.CurrencySymbolPosition
import com.nkuppan.expensemanager.domain.repository.CurrencyRepository
import com.nkuppan.expensemanager.utils.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import javax.inject.Inject

private val dollar = Currency(
    R.string.dollar_type,
    R.string.dollar_name,
    R.drawable.currency_dollar
)

private val euro = Currency(
    R.string.euro_type,
    R.string.euro_name,
    R.drawable.currency_euro
)

private val lira = Currency(
    R.string.lira_type,
    R.string.lira_name,
    R.drawable.currency_lira
)

private val pound = Currency(
    R.string.pound_type,
    R.string.pound_name,
    R.drawable.currency_pound
)

private val ruble = Currency(
    R.string.ruble_type,
    R.string.ruble_name,
    R.drawable.currency_ruble
)

private val rupee = Currency(
    R.string.rupee_type,
    R.string.rupee_name,
    R.drawable.currency_rupee
)

private val swissFranc = Currency(
    R.string.swiss_franc_type,
    R.string.swiss_franc_name,
    R.drawable.currency_franc
)

private val yen = Currency(
    R.string.yen_type,
    R.string.yen_name,
    R.drawable.currency_yen
)

private val yuan = Currency(
    R.string.yuan_type,
    R.string.yuan_name,
    R.drawable.currency_yuan
)

val availableCurrencies = listOf(
    dollar, euro, lira, pound, ruble, rupee, swissFranc, yen, yuan
)


class CurrencyRepositoryImpl @Inject constructor(
    private val dataStore: CurrencyDataStore,
    private val dispatchers: AppCoroutineDispatchers
) : CurrencyRepository {

    private fun getDefaultCurrency(): Currency {
        return Currency(
            R.string.default_currency_type,
            R.string.default_currency_name,
            R.drawable.currency_dollar
        )
    }

    override suspend fun saveCurrency(currency: Currency): Boolean = withContext(dispatchers.io) {
        dataStore.setCurrencySymbol(currency.type)
        dataStore.setCurrencySymbolPosition(currency.position.ordinal)
        true
    }

    override fun getSelectedCurrency(): Flow<Currency> {
        val defaultCurrency = getDefaultCurrency()
        val currencies = getAllCurrency()

        return dataStore.getCurrencySymbol(defaultCurrency.type).combine(
            dataStore.getCurrencySymbolPosition(CurrencySymbolPosition.PREFIX.ordinal)
        ) { symbol, position ->

            val selectedCurrency = currencies.find { currency ->
                currency.type == symbol
            } ?: defaultCurrency

            selectedCurrency.copy(position = CurrencySymbolPosition.values()[position])
        }
    }

    override fun getAllCurrency(): List<Currency> {
        return availableCurrencies
    }
}