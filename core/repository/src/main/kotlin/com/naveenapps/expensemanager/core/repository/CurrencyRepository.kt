package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {

    suspend fun saveCurrency(currency: Currency): Boolean

    fun getDefaultCurrency(): Currency

    fun getSelectedCurrency(): Flow<Currency>

    fun getFormattedCurrency(amount: Amount): Amount

    /**
     * The currency the user said a backup's amounts are in, set right before restoring it.
     * Backups from before multi-currency (schema 8) don't record one, so the migration that
     * runs on the next start uses this instead of guessing. Null clears it.
     */
    suspend fun setRestoreCurrencyCode(code: String?)

    suspend fun getRestoreCurrencyCode(): String?
}
