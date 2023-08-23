package com.nkuppan.expensemanager.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.nkuppan.expensemanager.core.model.FilterType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    suspend fun setAccountId(accountId: String) = dataStore.edit { preferences ->
        preferences[KEY_SELECTED_ACCOUNT_ID] = accountId
    }

    fun getAccountId(): Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_ACCOUNT_ID] ?: "-1"
    }

    suspend fun setFilterType(filterType: FilterType) = dataStore.edit { preferences ->
        preferences[KEY_FILTER_TYPE] = filterType.ordinal
    }

    fun getFilterType(): Flow<FilterType> = dataStore.data.map { preferences ->
        FilterType.values()[preferences[KEY_FILTER_TYPE] ?: FilterType.THIS_MONTH.ordinal]
    }

    suspend fun setCustomFilterStartDate(startDate: Long) = dataStore.edit { preferences ->
        preferences[KEY_CUSTOM_FILTER_START_DATE_TYPE] = startDate
    }

    fun getCustomFilterStartDate(): Flow<Long?> = dataStore.data.map { preferences ->
        preferences[KEY_CUSTOM_FILTER_START_DATE_TYPE]
    }

    suspend fun setCustomFilterEndDate(startDate: Long) = dataStore.edit { preferences ->
        preferences[KEY_CUSTOM_FILTER_END_DATE_TYPE] = startDate
    }

    fun getCustomFilterEndDate(): Flow<Long?> = dataStore.data.map { preferences ->
        preferences[KEY_CUSTOM_FILTER_END_DATE_TYPE]
    }

    suspend fun setReminder(reminder: Boolean) = dataStore.edit { preferences ->
        preferences[KEY_REMINDER] = reminder
    }

    fun isReminderOn(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_REMINDER] ?: true
    }

    companion object {
        private val KEY_CUSTOM_FILTER_START_DATE_TYPE =
            longPreferencesKey("custom_filter_start_date")
        private val KEY_CUSTOM_FILTER_END_DATE_TYPE = longPreferencesKey("custom_filter_end_date")

        private val KEY_FILTER_TYPE = intPreferencesKey("filter_type")
        private val KEY_SELECTED_ACCOUNT_ID = stringPreferencesKey("selected_account_id")
        private val KEY_REMINDER = booleanPreferencesKey("reminder")
    }
}
