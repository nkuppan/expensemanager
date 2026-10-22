package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.AutoBackupFrequency
import com.naveenapps.expensemanager.core.model.AutoBackupResult
import com.naveenapps.expensemanager.core.model.AutoBackupSettings
import kotlinx.coroutines.flow.Flow

/**
 * Automatic backups into a folder the user picked. Each run writes a new file in the same
 * format as a manual backup (so Restore reads it), then keeps only the newest
 * [KEEP_LAST_BACKUPS] automatic backups. Manual backups in the same folder are never touched.
 */
interface AutoBackupRepository {

    fun getSettings(): Flow<AutoBackupSettings>

    suspend fun setEnabled(enabled: Boolean)

    suspend fun setFrequency(frequency: AutoBackupFrequency)

    /**
     * Takes lasting access to the folder at [treeUri] (from the system folder picker) and makes
     * it the backup destination, releasing access to the previous one. Returns false if the
     * app couldn't get lasting access.
     */
    suspend fun setFolder(treeUri: String): Boolean

    /** Writes one backup now. Runs whether or not the schedule is on; needs a folder. */
    suspend fun backupNow(): AutoBackupResult

    /** Hides Home's failure banner until [untilMillis] ("Later"). */
    suspend fun snoozeFailureWarning(untilMillis: Long)

    /** Records that the current failure was announced, so it isn't notified again daily. */
    suspend fun markFailureNotified()

    companion object {
        const val KEEP_LAST_BACKUPS = 7

        /** How long "Later" on Home's failure banner hides it. */
        const val WARNING_SNOOZE_MILLIS = 3L * 24 * 60 * 60 * 1000
    }
}

/** Schedules the periodic background backup. Implemented with WorkManager in core:notification. */
interface AutoBackupScheduler {

    /**
     * Starts (or updates) the periodic backup for [frequency]. With [replaceExisting] false an
     * existing schedule is left exactly as it is (used on every app start).
     */
    fun schedule(frequency: AutoBackupFrequency, replaceExisting: Boolean)

    fun cancel()
}
