package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFormat
import com.naveenapps.expensemanager.core.model.TextPosition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CurrencyDataStore(private val dataStore: DataStore<Preferences>) {

    fun getCurrency(defaultCurrency: Currency): Flow<Currency> =
        dataStore.data.map { preferences ->
            val position =
                preferences[KEY_CURRENCY_SYMBOL_POSITION] ?: defaultCurrency.position.ordinal
            val format =
                preferences[KEY_CURRENCY_TEXT_FORMAT] ?: defaultCurrency.format.ordinal
            Currency(
                name = preferences[KEY_CURRENCY_NAME] ?: defaultCurrency.name,
                symbol = preferences[KEY_CURRENCY_SYMBOL] ?: defaultCurrency.symbol,
                position = TextPosition.entries[position],
                format = TextFormat.entries[format],
            )
        }

    suspend fun setCurrency(
        name: String,
        symbol: String,
        position: Int,
        format: Int,
    ) = dataStore.edit { preferences ->
        preferences[KEY_CURRENCY_NAME] = name
        preferences[KEY_CURRENCY_SYMBOL] = symbol
        preferences[KEY_CURRENCY_SYMBOL_POSITION] = position
        preferences[KEY_CURRENCY_TEXT_FORMAT] = format
    }

    companion object {
        private val KEY_CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol_string")
        private val KEY_CURRENCY_NAME = stringPreferencesKey("currency_name")
        private val KEY_CURRENCY_SYMBOL_POSITION = intPreferencesKey("currency_symbol_position")
        private val KEY_CURRENCY_TEXT_FORMAT = intPreferencesKey("currency_text_format")
    }
}
