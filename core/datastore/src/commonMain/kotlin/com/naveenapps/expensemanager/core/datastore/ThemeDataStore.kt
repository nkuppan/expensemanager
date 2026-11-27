package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThemeDataStore(private val dataStore: DataStore<Preferences>) {

    suspend fun setTheme(mode: Int) = dataStore.edit { preferences ->
        preferences[KEY_THEME_MODE] = mode
    }

    fun getTheme(defaultMode: Int): Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE] ?: defaultMode
    }

    companion object {
        private val KEY_THEME_MODE = intPreferencesKey("mode")
    }
}
