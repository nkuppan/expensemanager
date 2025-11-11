package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextPosition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CurrencyDataStore(private val dataStore: DataStore<Preferences>) {

    fun getCurrency(defaultCurrency: Currency): Flow<Currency> =
        dataStore.data.map { preferences ->
            val position =
                preferences[KEY_CURRENCY_SYMBOL_POSITION] ?: defaultCurrency.position.ordinal
            Currency(
                name = preferences[KEY_CURRENCY_NAME] ?: defaultCurrency.name,
                symbol = preferences[KEY_CURRENCY_SYMBOL] ?: defaultCurrency.symbol,
                position = TextPosition.entries[position],
            )
        }

    suspend fun setCurrency(
        name: String,
        symbol: String,
        position: Int,
    ) = dataStore.edit { preferences ->
        preferences[KEY_CURRENCY_NAME] = name
        preferences[KEY_CURRENCY_SYMBOL] = symbol
        preferences[KEY_CURRENCY_SYMBOL_POSITION] = position
    }

    companion object {
        private val KEY_CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol_string")
        private val KEY_CURRENCY_NAME = stringPreferencesKey("currency_name")
        private val KEY_CURRENCY_SYMBOL_POSITION = intPreferencesKey("currency_symbol_position")
    }
}
