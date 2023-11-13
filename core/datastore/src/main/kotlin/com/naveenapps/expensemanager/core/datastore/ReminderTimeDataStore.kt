package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.naveenapps.expensemanager.core.model.ReminderTimeState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderTimeDataStore(private val dataStore: DataStore<Preferences>) {

    suspend fun setReminderTime(reminderTimeState: ReminderTimeState) =
        dataStore.edit { preferences ->
            preferences[KEY_REMINDER_TIME_STATE] =
                "${reminderTimeState.hour}:${reminderTimeState.minute}:${reminderTimeState.is24Hour}"
        }

    fun getReminderTime(defaultSymbol: String): Flow<ReminderTimeState> =
        dataStore.data.map { preferences ->
            val timers = (preferences[KEY_REMINDER_TIME_STATE] ?: defaultSymbol).split(":")
            ReminderTimeState(
                hour = timers[0].toIntOrNull() ?: 10,
                minute = timers[1].toIntOrNull() ?: 0,
                is24Hour = timers[2].toBooleanStrictOrNull() ?: false,
            )
        }

    suspend fun setReminder(reminder: Boolean) = dataStore.edit { preferences ->
        preferences[KEY_REMINDER] = reminder
    }

    fun isReminderOn(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_REMINDER] ?: true
    }

    companion object {
        private val KEY_REMINDER_TIME_STATE = stringPreferencesKey("reminder_time")
        private val KEY_REMINDER = booleanPreferencesKey("reminder")
    }
}
