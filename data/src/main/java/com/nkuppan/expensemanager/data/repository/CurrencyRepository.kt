package com.nkuppan.expensemanager.data.repository

import com.nkuppan.expensemanager.core.model.Currency
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {

    suspend fun saveCurrency(currency: Currency): Boolean

    fun getSelectedCurrency(): Flow<Currency>

    fun getAllCurrency(): List<Currency>
}