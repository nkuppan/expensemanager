package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.naveenapps.expensemanager.core.model.AutoBackupError
import com.naveenapps.expensemanager.core.model.AutoBackupFrequency
import com.naveenapps.expensemanager.core.model.AutoBackupSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Automatic backup settings. Must be given the device-local DataStore (see
 * [DeviceLocalDataStore]): the folder permission belongs to this install, so restoring these
 * settings onto a new phone would point at a folder the app can't open there.
 */
class AutoBackupDataStore(private val dataStore: DataStore<Preferences>) {

    fun getSettings(): Flow<AutoBackupSettings> = dataStore.data.map { preferences ->
        AutoBackupSettings(
            isEnabled = preferences[KEY_ENABLED] ?: false,
            folderUri = preferences[KEY_FOLDER_URI],
            folderName = preferences[KEY_FOLDER_NAME],
            frequency = preferences[KEY_FREQUENCY]
                ?.let { name -> AutoBackupFrequency.entries.find { it.name == name } }
                ?: AutoBackupFrequency.DAILY,
            lastBackupAt = preferences[KEY_LAST_BACKUP_AT],
            lastAutoBackupAt = preferences[KEY_LAST_AUTO_BACKUP_AT],
            lastError = preferences[KEY_LAST_ERROR]
                ?.let { name -> AutoBackupError.entries.find { it.name == name } },
            warningSnoozedUntil = preferences[KEY_WARNING_SNOOZED_UNTIL],
            isFailureNotified = preferences[KEY_FAILURE_NOTIFIED] ?: false,
        )
    }

    suspend fun setEnabled(enabled: Boolean) = dataStore.edit { it[KEY_ENABLED] = enabled }

    suspend fun setFrequency(frequency: AutoBackupFrequency) = dataStore.edit { it[KEY_FREQUENCY] = frequency.name }

    /** A new folder also clears the last error, which was about the old one. */
    suspend fun setFolder(uri: String, name: String?) = dataStore.edit { preferences ->
        preferences[KEY_FOLDER_URI] = uri
        if (name != null) preferences[KEY_FOLDER_NAME] = name else preferences.remove(KEY_FOLDER_NAME)
        preferences.clearFailure()
    }

    suspend fun recordSuccess(timeMillis: Long, isAuto: Boolean) = dataStore.edit { preferences ->
        preferences[KEY_LAST_BACKUP_AT] = timeMillis
        if (isAuto) preferences[KEY_LAST_AUTO_BACKUP_AT] = timeMillis
        preferences.clearFailure()
    }

    suspend fun recordError(error: AutoBackupError) = dataStore.edit { it[KEY_LAST_ERROR] = error.name }

    suspend fun snoozeWarning(untilMillis: Long) = dataStore.edit { it[KEY_WARNING_SNOOZED_UNTIL] = untilMillis }

    suspend fun setFailureNotified() = dataStore.edit { it[KEY_FAILURE_NOTIFIED] = true }

    /** A fixed problem resets the warning state, so the next failure is announced again. */
    private fun MutablePreferences.clearFailure() {
        remove(KEY_LAST_ERROR)
        remove(KEY_WARNING_SNOOZED_UNTIL)
        remove(KEY_FAILURE_NOTIFIED)
    }

    companion object {
        private val KEY_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        private val KEY_FOLDER_URI = stringPreferencesKey("auto_backup_folder_uri")
        private val KEY_FOLDER_NAME = stringPreferencesKey("auto_backup_folder_name")
        private val KEY_FREQUENCY = stringPreferencesKey("auto_backup_frequency")
        private val KEY_LAST_BACKUP_AT = longPreferencesKey("auto_backup_last_backup_at")
        private val KEY_LAST_AUTO_BACKUP_AT = longPreferencesKey("auto_backup_last_auto_backup_at")
        private val KEY_LAST_ERROR = stringPreferencesKey("auto_backup_last_error")
        private val KEY_WARNING_SNOOZED_UNTIL = longPreferencesKey("auto_backup_warning_snoozed_until")
        private val KEY_FAILURE_NOTIFIED = booleanPreferencesKey("auto_backup_failure_notified")
    }
}
