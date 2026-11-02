package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.naveenapps.expensemanager.core.model.ExchangeMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExchangeMethodDataStore(private val dataStore: DataStore<Preferences>) {

    /** The method the user picked, or null while they haven't picked one. */
    fun getMethod(): Flow<ExchangeMethod?> = dataStore.data.map { preferences ->
        preferences[KEY_METHOD]?.let { name ->
            runCatching { ExchangeMethod.valueOf(name) }.getOrNull()
        }
    }

    suspend fun setMethod(method: ExchangeMethod) {
        dataStore.edit { preferences -> preferences[KEY_METHOD] = method.name }
    }

    companion object {
        private val KEY_METHOD = stringPreferencesKey("exchange_method")
    }
}
