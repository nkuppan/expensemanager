package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {

    suspend fun saveCurrency(currency: Currency): Boolean

    fun getSelectedCurrency(): Flow<Currency>

    fun getAllCurrency(): List<Currency>

    fun getFormattedCurrency(amount: Amount): Amount
}