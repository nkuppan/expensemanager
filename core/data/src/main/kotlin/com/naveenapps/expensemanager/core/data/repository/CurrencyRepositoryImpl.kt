package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import com.naveenapps.expensemanager.core.common.R
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.data.utils.getCurrency
import com.naveenapps.expensemanager.core.datastore.CurrencyDataStore
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFormat
import com.naveenapps.expensemanager.core.model.TextPosition
import com.naveenapps.expensemanager.core.model.isPrefix
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val MINUS_SYMBOL = "-"

class CurrencyRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: CurrencyDataStore,
    private val dispatchers: AppCoroutineDispatchers
) : CurrencyRepository {

    override fun getDefaultCurrency(): Currency {
        return Currency(
            name = context.getString(R.string.default_currency_name),
            symbol = context.getString(R.string.default_currency_type),
            position = TextPosition.SUFFIX,
            format = TextFormat.NONE
        )
    }

    override suspend fun saveCurrency(currency: Currency): Boolean = withContext(dispatchers.io) {
        dataStore.setCurrency(
            name = currency.name,
            symbol = currency.symbol,
            position = currency.position.ordinal,
            format = currency.format.ordinal,
        )
        true
    }

    override fun getSelectedCurrency(): Flow<Currency> {
        return dataStore.getCurrency(defaultCurrency = getDefaultCurrency())
    }

    override fun getFormattedCurrency(amount: Amount): Amount {
        val currency = amount.currency ?: getDefaultCurrency()
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