package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * State that belongs to this device, not to the user's data, and so must NOT be restored onto a
 * new phone by Android Auto Backup. Lives in its own DataStore file, which backup_rules.xml and
 * data_extraction_rules.xml deliberately leave out.
 *
 * Example: whether we've already shown the notification explainer. Notification permission is
 * per-device, so a user restoring onto a new phone needs to be asked again there.
 */
class DeviceLocalDataStore(private val dataStore: DataStore<Preferences>) {

    suspend fun setNotificationPrimerShown() = dataStore.edit { preferences ->
        preferences[KEY_NOTIFICATION_PRIMER_SHOWN] = true
    }

    fun isNotificationPrimerShown(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_NOTIFICATION_PRIMER_SHOWN] ?: false
    }

    companion object {
        /** File name — keep in sync with the backup rules, which must exclude it. */
        const val FILE_NAME = "expense_manager_device_local"

        private val KEY_NOTIFICATION_PRIMER_SHOWN =
            booleanPreferencesKey("notification_primer_shown")
    }
}
