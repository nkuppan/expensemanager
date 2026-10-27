package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.AutoBackupFrequency
import com.naveenapps.expensemanager.core.model.AutoBackupResult
import com.naveenapps.expensemanager.core.model.AutoBackupSettings
import com.naveenapps.expensemanager.core.model.BackupFile
import com.naveenapps.expensemanager.core.model.BackupKind
import kotlinx.coroutines.flow.Flow

/**
 * Backups into a folder the user picked, or into the app's own storage until they pick one.
 * Each run writes a new file in the same format as a backup file (so Restore reads it). After an
 * automatic run only the newest [KEEP_LAST_BACKUPS] automatic backups are kept; "Back up now"
 * backups are never deleted.
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

    /** Writes one backup now, into the folder or, without one, the app's own storage. */
    suspend fun backupNow(kind: BackupKind = BackupKind.MANUAL): AutoBackupResult

    /** Backups in the folder and in the app's own storage, newest first. */
    suspend fun getBackups(): List<BackupFile>

    /**
     * Copies [backup] somewhere other apps can read it and returns a content:// URI to share,
     * or null if that failed. With a [passphrase] the copy is encrypted with it.
     */
    suspend fun prepareShare(backup: BackupFile, passphrase: String?): String?

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
