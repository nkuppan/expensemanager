package com.nkuppan.expensemanager.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.model.DateRangeFilterType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    suspend fun setCategoryTypes(categoryTypes: List<CategoryType>?) =
        dataStore.edit { preferences ->
            preferences[KEY_CATEGORY_TYPES] =
                categoryTypes?.map { it.ordinal.toString() }?.toSet() ?: emptySet()
        }

    fun getCategoryTypes(): Flow<List<CategoryType>?> = dataStore.data.map { preferences ->
        preferences[KEY_CATEGORY_TYPES]?.toList()?.map { CategoryType.values()[it.toInt()] }
            ?: emptyList()
    }

    suspend fun setAccounts(accounts: List<String>?) = dataStore.edit { preferences ->
        preferences[KEY_SELECTED_ACCOUNTS] = accounts?.toSet() ?: emptySet()
    }

    fun getAccounts(): Flow<List<String>?> = dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_ACCOUNTS]?.toList() ?: emptyList()
    }

    suspend fun setCategories(categories: List<String>?) = dataStore.edit { preferences ->
        preferences[KEY_SELECTED_CATEGORIES] = categories?.toSet() ?: emptySet()
    }

    fun getCategories(): Flow<List<String>?> = dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_CATEGORIES]?.toList() ?: emptyList()
    }

    suspend fun setFilterType(dateRangeFilterType: DateRangeFilterType) =
        dataStore.edit { preferences ->
            preferences[KEY_DATE_FILTER_TYPE] = dateRangeFilterType.ordinal
        }

    fun getFilterType(): Flow<DateRangeFilterType> = dataStore.data.map { preferences ->
        DateRangeFilterType.values()[preferences[KEY_DATE_FILTER_TYPE]
            ?: DateRangeFilterType.THIS_MONTH.ordinal]
    }

    suspend fun setCustomFilterStartDate(startDate: Long) = dataStore.edit { preferences ->
        preferences[KEY_CUSTOM_RANGE_START_DATE_TYPE] = startDate
    }

    fun getCustomFilterStartDate(): Flow<Long?> = dataStore.data.map { preferences ->
        preferences[KEY_CUSTOM_RANGE_START_DATE_TYPE]
    }

    suspend fun setCustomFilterEndDate(startDate: Long) = dataStore.edit { preferences ->
        preferences[KEY_CUSTOM_RANGE_END_DATE_TYPE] = startDate
    }

    fun getCustomFilterEndDate(): Flow<Long?> = dataStore.data.map { preferences ->
        preferences[KEY_CUSTOM_RANGE_END_DATE_TYPE]
    }

    suspend fun setReminder(reminder: Boolean) = dataStore.edit { preferences ->
        preferences[KEY_REMINDER] = reminder
    }

    fun isReminderOn(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_REMINDER] ?: true
    }

    suspend fun setFilterEnabled(enableFilter: Boolean) = dataStore.edit { preferences ->
        preferences[KEY_FILTER_ENABLED] = enableFilter
    }

    fun isFilterEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_FILTER_ENABLED] ?: false
    }

    companion object {
        private val KEY_REMINDER = booleanPreferencesKey("reminder")

        private val KEY_FILTER_ENABLED = booleanPreferencesKey("filter_enabled")

        private val KEY_DATE_FILTER_TYPE = intPreferencesKey("date_filter_type")
        private val KEY_CUSTOM_RANGE_START_DATE_TYPE = longPreferencesKey("custom_range_start_date")
        private val KEY_CUSTOM_RANGE_END_DATE_TYPE = longPreferencesKey("custom_range_end_date")
        private val KEY_SELECTED_ACCOUNTS = stringSetPreferencesKey("selected_accounts")
        private val KEY_SELECTED_CATEGORIES = stringSetPreferencesKey("selected_categories")
        private val KEY_CATEGORY_TYPES = stringSetPreferencesKey("category_types")
    }
}
