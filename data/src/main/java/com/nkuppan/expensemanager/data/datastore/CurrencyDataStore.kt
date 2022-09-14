package com.nkuppan.expensemanager.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CurrencyDataStore(private val dataStore: DataStore<Preferences>) {

    suspend fun setCurrencySymbol(currency: Int) = dataStore.edit { preferences ->
        preferences[KEY_CURRENCY_SYMBOL] = currency
    }

    fun getCurrencySymbol(defaultSymbol: Int): Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_CURRENCY_SYMBOL] ?: defaultSymbol
    }

    companion object {
        private val KEY_CURRENCY_SYMBOL = intPreferencesKey("currency_symbol")
    }
}
