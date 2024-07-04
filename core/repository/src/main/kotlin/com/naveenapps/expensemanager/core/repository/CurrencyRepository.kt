package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {

    suspend fun saveCurrency(currency: Currency): Boolean

    fun getDefaultCurrency(): Currency

    fun getSelectedCurrency(): Flow<Currency>

    fun getFormattedCurrency(amount: Amount): Amount
}
