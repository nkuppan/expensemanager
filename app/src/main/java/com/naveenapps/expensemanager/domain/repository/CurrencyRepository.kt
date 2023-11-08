package com.naveenapps.expensemanager.domain.repository

import com.naveenapps.expensemanager.domain.model.Currency
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {

    suspend fun saveCurrency(currency: Currency): Boolean

    fun getSelectedCurrency(): Flow<Currency>

    fun getAllCurrency(): List<Currency>
}