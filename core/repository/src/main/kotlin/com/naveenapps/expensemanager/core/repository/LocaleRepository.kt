package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.AppLocale
import kotlinx.coroutines.flow.Flow

interface LocaleRepository {

    /**
     * Storing the selected locale and applying it immediately
     */
    suspend fun saveLocale(locale: AppLocale): Boolean

    /**
     * Re-applying the previously persisted locale, called on app startup
     */
    suspend fun applyLocale()

    /**
     * Reading the selected locale
     */
    fun getSelectedLocale(): Flow<AppLocale>

    /**
     * Reading the list of locales available for selection
     */
    fun getLocales(): List<AppLocale>
}
