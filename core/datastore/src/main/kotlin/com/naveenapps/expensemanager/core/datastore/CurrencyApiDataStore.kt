package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.naveenapps.expensemanager.core.model.CurrencyApiPreset
import com.naveenapps.expensemanager.core.model.CurrencyApiProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CurrencyApiDataStore(private val dataStore: DataStore<Preferences>) {

    fun getProfile(): Flow<CurrencyApiProfile?> = dataStore.data.map { preferences ->
        val presetName = preferences[KEY_PRESET] ?: return@map null
        val preset = runCatching { CurrencyApiPreset.valueOf(presetName) }
            .getOrDefault(CurrencyApiPreset.FRANKFURTER)
        CurrencyApiProfile(
            preset = preset,
            baseUrl = preferences[KEY_BASE_URL] ?: preset.defaultBaseUrl,
            apiKey = preferences[KEY_API_KEY] ?: "",
        )
    }

    suspend fun saveProfile(profile: CurrencyApiProfile) {
        dataStore.edit { preferences ->
            preferences[KEY_PRESET] = profile.preset.name
            preferences[KEY_BASE_URL] = profile.baseUrl
            preferences[KEY_API_KEY] = profile.apiKey
        }
    }

    companion object {
        private val KEY_PRESET = stringPreferencesKey("currency_api_preset")
        private val KEY_BASE_URL = stringPreferencesKey("currency_api_base_url")
        private val KEY_API_KEY = stringPreferencesKey("currency_api_key")
    }
}
