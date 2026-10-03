package com.naveenapps.expensemanager.core.database

import android.content.Context
import android.util.Log
import java.io.File

/**
 * Restoring a backup never touches the live database file while Room has it open.
 *
 * The restore flow validates the backup and [stage]s it here; the app then restarts, and
 * [applyIfPending] swaps the file in from the database module, before Room opens it. Doing the
 * swap while the database was open (closing it under running Flow collectors, then overwriting the
 * file) is what the old backup library did, and it was fragile.
 *
 * Staged files live in noBackupFilesDir, so Android Auto Backup never picks them up.
 */
object PendingDatabaseRestore {

    private const val TAG = "PendingDatabaseRestore"
    private const val DIRECTORY = "pending_restore"
    private val SIDECAR_SUFFIXES = listOf("-wal", "-shm", "-journal")

    fun stage(context: Context, databaseName: String, databaseBytes: ByteArray) {
        val directory = File(context.noBackupFilesDir, DIRECTORY).apply { mkdirs() }
        val temp = File(directory, "$databaseName.tmp")
        temp.writeBytes(databaseBytes)
        val target = File(directory, databaseName)
        if (!temp.renameTo(target)) {
            temp.copyTo(target, overwrite = true)
            temp.delete()
        }
    }

    /** Must run before Room opens [databaseName]. Returns true if a staged backup was applied. */
    fun applyIfPending(context: Context, databaseName: String): Boolean {
        val staged = File(File(context.noBackupFilesDir, DIRECTORY), databaseName)
        if (!staged.exists()) return false
        return try {
            val databaseFile = context.getDatabasePath(databaseName)
            databaseFile.parentFile?.mkdirs()
            // The old WAL/SHM belong to the database being replaced; replaying them onto the
            // restored file would corrupt it.
            SIDECAR_SUFFIXES.forEach { File(databaseFile.path + it).delete() }
            if (!staged.renameTo(databaseFile)) {
                staged.copyTo(databaseFile, overwrite = true)
                staged.delete()
            }
            true
        } catch (e: Exception) {
            // Never block app start on this: keep the current data and drop the staged copy.
            Log.e(TAG, "Unable to apply the staged restore", e)
            staged.delete()
            false
        }
    }
}
