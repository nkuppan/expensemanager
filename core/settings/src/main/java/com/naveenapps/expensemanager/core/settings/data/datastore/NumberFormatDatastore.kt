package com.naveenapps.expensemanager.core.settings.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.naveenapps.expensemanager.core.settings.domain.model.NumberFormatType
import com.naveenapps.expensemanager.core.settings.domain.model.toEnumValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NumberFormatSettingsDatastore(
    private val dataStore: DataStore<Preferences>
) {
    suspend fun setNumberFormatType(type: NumberFormatType) = dataStore.edit { preferences ->
        preferences[KEY_NUMBER_FORMAT_TYPE] = type.ordinal
    }

    fun getNumberFormatType(): Flow<NumberFormatType> = dataStore.data.map { preferences ->
        toEnumValue<NumberFormatType>(preferences[KEY_NUMBER_FORMAT_TYPE] ?: 0)
    }

    companion object {
        private val KEY_NUMBER_FORMAT_TYPE = intPreferencesKey("number_format_type")
    }
}