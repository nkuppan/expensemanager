package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.naveenapps.expensemanager.core.model.ReminderTimeState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderTimeDataStore(private val dataStore: DataStore<Preferences>) {

    suspend fun setReminderTime(reminderTimeState: ReminderTimeState) = dataStore.edit { preferences ->
        preferences[KEY_REMINDER_TIME_STATE] =
            "${reminderTimeState.hour}:${reminderTimeState.minute}:${reminderTimeState.is24Hour}"
    }

    fun getReminderTime(defaultSymbol: String): Flow<ReminderTimeState> = dataStore.data.map { preferences ->
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

    // "Upcoming payment alerts": a heads-up the evening before a weekly, monthly or yearly recurring
    // entry is added. On by default, separate from the daily logging reminder.
    suspend fun setUpcomingAlertOn(on: Boolean) = dataStore.edit { preferences ->
        preferences[KEY_UPCOMING_ALERT] = on
    }

    fun isUpcomingAlertOn(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_UPCOMING_ALERT] ?: true
    }

    /** Epoch day the last heads-up was shown, so a second run the same evening stays quiet. */
    suspend fun setLastUpcomingAlertDay(epochDay: Long) = dataStore.edit { preferences ->
        preferences[KEY_LAST_UPCOMING_ALERT_DAY] = epochDay
    }

    fun getLastUpcomingAlertDay(): Flow<Long?> = dataStore.data.map { preferences ->
        preferences[KEY_LAST_UPCOMING_ALERT_DAY]
    }

    /** When the next daily reminder is due (epoch millis), to tell later whether it ran. */
    suspend fun setReminderScheduledFor(millis: Long) = dataStore.edit { preferences ->
        preferences[KEY_REMINDER_SCHEDULED_FOR] = millis
    }

    fun getReminderScheduledFor(): Flow<Long?> = dataStore.data.map { preferences ->
        preferences[KEY_REMINDER_SCHEDULED_FOR]
    }

    /** When the daily reminder's job last actually ran (epoch millis), shown or not. */
    suspend fun setReminderLastRunAt(millis: Long) = dataStore.edit { preferences ->
        preferences[KEY_REMINDER_LAST_RUN_AT] = millis
    }

    fun getReminderLastRunAt(): Flow<Long?> = dataStore.data.map { preferences ->
        preferences[KEY_REMINDER_LAST_RUN_AT]
    }

    companion object {
        private val KEY_REMINDER_SCHEDULED_FOR = longPreferencesKey("reminder_scheduled_for")
        private val KEY_REMINDER_LAST_RUN_AT = longPreferencesKey("reminder_last_run_at")
        private val KEY_UPCOMING_ALERT = booleanPreferencesKey("upcoming_alert")
        private val KEY_LAST_UPCOMING_ALERT_DAY = longPreferencesKey("last_upcoming_alert_day")
        private val KEY_REMINDER_TIME_STATE = stringPreferencesKey("reminder_time")
        private val KEY_REMINDER = booleanPreferencesKey("reminder")
    }
}
