package com.naveenapps.expensemanager.core.data.repository.backup

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Naming, recognition and retention for backups the app writes itself. Names carry a sortable timestamp (pinned to
 * Locale.US, since they're parsed back), and a prefix that manual backups
 * ("expense-manager-<timestamp>...") never start with, so pruning can't delete them.
 */
internal object AutoBackupFiles {

    const val PREFIX = "expense-manager-auto-"

    /** "Back up now" uses the same name as a backup file saved with the picker. */
    const val MANUAL_PREFIX = "expense-manager-"
    const val EXTENSION = "sqlite3.aes"

    private const val TIMESTAMP_PATTERN = "yyyy-MM-dd-HHmmss"
    private val TIMESTAMP = Regex("""\d{4}-\d{2}-\d{2}-\d{6}""")

    fun fileName(date: Date): String = "$PREFIX${stamp(date)}.$EXTENSION"

    fun manualFileName(date: Date): String = "$MANUAL_PREFIX${stamp(date)}.$EXTENSION"

    private fun stamp(date: Date): String = SimpleDateFormat(TIMESTAMP_PATTERN, Locale.US).format(date)

    /** What a backup's name says about it, or null for files the app didn't write. */
    fun parse(name: String): Parsed? {
        val (isAuto, start) = when {
            isAutoBackup(name) -> true to PREFIX.length
            name.startsWith(MANUAL_PREFIX) && TIMESTAMP.matchesAt(name, MANUAL_PREFIX.length) -> false to MANUAL_PREFIX.length
            else -> return null
        }
        val createdAt = runCatching {
            SimpleDateFormat(TIMESTAMP_PATTERN, Locale.US).apply { isLenient = false }
                .parse(name.substring(start, start + TIMESTAMP_LENGTH))?.time
        }.getOrNull() ?: return null
        return Parsed(isAuto = isAuto, createdAt = createdAt)
    }

    data class Parsed(val isAuto: Boolean, val createdAt: Long)

    /**
     * True for files this feature created. Only the prefix and timestamp are checked: some
     * storage providers append " (1)" or their own extension to the name they're given.
     */
    fun isAutoBackup(name: String): Boolean = name.startsWith(PREFIX) &&
        TIMESTAMP.matchesAt(name, PREFIX.length)

    /** Automatic backups beyond the newest [keep], oldest last in the result's order. */
    fun toDelete(names: List<String>, keep: Int): List<String> = names
        .filter(::isAutoBackup)
        .sortedByDescending { it.substring(PREFIX.length, PREFIX.length + TIMESTAMP_LENGTH) }
        .drop(keep.coerceAtLeast(0))

    private const val TIMESTAMP_LENGTH = 17
}
