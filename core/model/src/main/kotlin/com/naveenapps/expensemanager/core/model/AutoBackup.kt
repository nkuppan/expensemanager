package com.naveenapps.expensemanager.core.model

/** How often automatic backups run. */
enum class AutoBackupFrequency(val intervalDays: Long) {
    DAILY(1),
    WEEKLY(7),
}

/** Why the most recent automatic backup didn't complete. */
enum class AutoBackupError {
    /** The chosen folder was deleted, or the app's access to it was revoked. */
    FOLDER_UNAVAILABLE,

    /** The folder was reachable but writing the backup failed (it will be retried). */
    WRITE_FAILED,
}

/** Outcome of a single automatic (or "Back up now") run. */
enum class AutoBackupResult {
    SUCCESS,

    /** No folder chosen yet, so there was nothing to do. */
    SKIPPED,
    FOLDER_UNAVAILABLE,
    FAILED,
}

/**
 * Automatic local backup settings. Stored per device (never restored onto another phone): the
 * folder access is a permission granted to this install only.
 */
data class AutoBackupSettings(
    val isEnabled: Boolean = false,
    /** A Storage Access Framework tree URI the app holds a persistable permission for. */
    val folderUri: String? = null,
    /** The folder's display name, for showing in Settings. */
    val folderName: String? = null,
    val frequency: AutoBackupFrequency = AutoBackupFrequency.DAILY,
    /** Epoch millis of the last successful automatic or manual-now backup. */
    val lastBackupAt: Long? = null,
    /** Set when the latest attempt failed; cleared on the next success or folder change. */
    val lastError: AutoBackupError? = null,
    /** Home's failure banner stays hidden until this time (epoch millis) after "Later". */
    val warningSnoozedUntil: Long? = null,
    /** A notification was already shown for the current run of failures. */
    val isFailureNotified: Boolean = false,
)

/**
 * Whether Home should show the "automatic backup failed" banner: backups are on, the last one
 * failed, and the user hasn't snoozed the warning.
 */
fun AutoBackupSettings.needsAttention(nowMillis: Long): Boolean = isEnabled &&
    lastError != null &&
    (warningSnoozedUntil ?: 0L) <= nowMillis
