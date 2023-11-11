package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.data.R
import com.naveenapps.expensemanager.core.data.utils.getCurrency
import com.naveenapps.expensemanager.core.datastore.CurrencyDataStore
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.CurrencySymbolPosition
import com.naveenapps.expensemanager.core.model.isPrefix
import dagger.hilt.android.qualifiers.ApplicationContext
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


private const val MINUS_SYMBOL = "-"

class CurrencyRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
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
            dataStore.getCurrencySymbolPosition(CurrencySymbolPosition.SUFFIX.ordinal)
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

    override fun getFormattedCurrency(amount: Amount): Amount {
        val currency = amount.currency ?: dollar
        return amount.copy(
            amountString = getCurrency(
                context = context,
                currency = currency,
                amount = amount.amount
            ).let {
                return@let if (currency.position.isPrefix() && it.contains(MINUS_SYMBOL)) {
                    "${MINUS_SYMBOL}${it.replace(MINUS_SYMBOL, "")}"
                } else {
                    it
                }
            }
        )
    }
}