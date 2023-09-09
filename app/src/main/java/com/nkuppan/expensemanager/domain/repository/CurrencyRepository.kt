package com.nkuppan.expensemanager.domain.repository

import com.nkuppan.expensemanager.domain.model.Currency
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {

    suspend fun saveCurrency(currency: Currency): Boolean

    fun getSelectedCurrency(): Flow<Currency>

    fun getAllCurrency(): List<Currency>
}