package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocaleDataStore(private val dataStore: DataStore<Preferences>) {

    suspend fun setLocaleTag(tag: String) = dataStore.edit { preferences ->
        preferences[KEY_LOCALE_TAG] = tag
    }

    fun getLocaleTag(defaultTag: String): Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_LOCALE_TAG] ?: defaultTag
    }

    companion object {
        private val KEY_LOCALE_TAG = stringPreferencesKey("locale_tag")
    }
}
