package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.naveenapps.expensemanager.core.model.DateRangeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DateRangeDataStore @Inject constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun setFilterType(dateRangeType: DateRangeType) =
        dataStore.edit { preferences ->
            preferences[KEY_DATE_FILTER_TYPE] = dateRangeType.ordinal
        }

    fun getFilterType(): Flow<DateRangeType> = dataStore.data.map { preferences ->
        DateRangeType.entries[preferences[KEY_DATE_FILTER_TYPE]
            ?: DateRangeType.THIS_MONTH.ordinal]
    }

    suspend fun setDateRanges(startDate: Long, endDate: Long) = dataStore.edit { preferences ->
        preferences[KEY_DATE_RANGE_START_TIME_TYPE] = startDate
        preferences[KEY_DATE_RANGE_END_TIME_TYPE] = endDate
    }

    fun getDateRanges(): Flow<List<Long>?> =
        dataStore.data.map { preferences ->
            val startDate = preferences[KEY_DATE_RANGE_START_TIME_TYPE]
            val endDate = preferences[KEY_DATE_RANGE_END_TIME_TYPE]
            if (startDate != null && endDate != null) {
                listOf(startDate, endDate)
            } else {
                null
            }
        }

    companion object {
        private val KEY_DATE_FILTER_TYPE = intPreferencesKey("date_filter_type")
        private val KEY_DATE_RANGE_START_TIME_TYPE = longPreferencesKey("date_range_start_date")
        private val KEY_DATE_RANGE_END_TIME_TYPE = longPreferencesKey("date_range_end_date")
    }
}