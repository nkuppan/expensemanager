package com.naveenapps.expensemanager.core.data.repository.backup

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Naming and retention for automatic backups. Names carry a sortable timestamp (pinned to
 * Locale.US, since they're parsed back), and a prefix that manual backups
 * ("expense-manager-<timestamp>...") never start with, so pruning can't delete them.
 */
internal object AutoBackupFiles {

    const val PREFIX = "expense-manager-auto-"
    const val EXTENSION = "sqlite3.aes"

    private const val TIMESTAMP_PATTERN = "yyyy-MM-dd-HHmmss"
    private val TIMESTAMP = Regex("""\d{4}-\d{2}-\d{2}-\d{6}""")

    fun fileName(date: Date): String {
        val stamp = SimpleDateFormat(TIMESTAMP_PATTERN, Locale.US).format(date)
        return "$PREFIX$stamp.$EXTENSION"
    }

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
