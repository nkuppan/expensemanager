package com.naveenapps.expensemanager.core.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
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
 * The database being replaced is moved aside, not deleted. If Room then fails to open or migrate
 * the restored file, [rollback] puts it back, so a bad backup can never leave the app unable to
 * start with the user's data gone. Once Room has opened the restored file, [confirmRestored]
 * drops the old copy.
 *
 * Staged and previous files live in noBackupFilesDir, so Android Auto Backup never picks them up.
 */
object PendingDatabaseRestore {

    private const val TAG = "PendingDatabaseRestore"
    private const val DIRECTORY = "pending_restore"
    private const val PREVIOUS_DIRECTORY = "previous_database"
    private val SIDECAR_SUFFIXES = listOf("-wal", "-shm", "-journal")
    private val DATABASE_SUFFIXES = listOf("") + SIDECAR_SUFFIXES

    /** The oldest schema version the registered migrations can bring up to date. */
    const val OLDEST_RESTORABLE_VERSION = 2

    /** Tables every schema version since [OLDEST_RESTORABLE_VERSION] has. */
    private val REQUIRED_TABLES = setOf("account", "category", "transaction")

    /**
     * Returns the required tables missing from the SQLite file in [databaseBytes], or all of them
     * if it can't be opened. An empty result means it looks like an Expense Manager database.
     */
    fun missingRequiredTables(context: Context, databaseBytes: ByteArray): Set<String> {
        val temp = File.createTempFile("restore-check", ".db", context.cacheDir)
        return try {
            temp.writeBytes(databaseBytes)
            SQLiteDatabase.openDatabase(temp.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                val present = mutableSetOf<String>()
                db.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table'", null).use { cursor ->
                    while (cursor.moveToNext()) present += cursor.getString(0)
                }
                REQUIRED_TABLES - present
            }
        } catch (e: Exception) {
            Log.w(TAG, "Backup file couldn't be opened as a database", e)
            REQUIRED_TABLES
        } finally {
            DATABASE_SUFFIXES.forEach { File(temp.path + it).delete() }
        }
    }

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
            // Move the current database aside together with its WAL/SHM: the WAL may hold changes
            // not yet checkpointed, and replaying it onto the restored file would corrupt it.
            val previous = File(context.noBackupFilesDir, PREVIOUS_DIRECTORY).apply {
                deleteRecursively()
                mkdirs()
            }
            DATABASE_SUFFIXES.forEach { suffix ->
                val file = File(databaseFile.path + suffix)
                if (file.exists()) moveFile(file, File(previous, databaseName + suffix))
            }
            moveFile(staged, databaseFile)
            true
        } catch (e: Exception) {
            // Never block app start on this: keep the current data and drop the staged copy.
            Log.e(TAG, "Unable to apply the staged restore", e)
            rollback(context, databaseName)
            staged.delete()
            false
        }
    }

    /** Room opened the restored database: the copy kept for [rollback] is no longer needed. */
    fun confirmRestored(context: Context) {
        File(context.noBackupFilesDir, PREVIOUS_DIRECTORY).deleteRecursively()
    }

    /**
     * Puts back the database that [applyIfPending] moved aside. Call after closing Room.
     * Returns false if there was nothing to roll back to.
     */
    fun rollback(context: Context, databaseName: String): Boolean {
        val previous = File(context.noBackupFilesDir, PREVIOUS_DIRECTORY)
        if (!File(previous, databaseName).exists()) return false
        val databaseFile = context.getDatabasePath(databaseName)
        DATABASE_SUFFIXES.forEach { File(databaseFile.path + it).delete() }
        DATABASE_SUFFIXES.forEach { suffix ->
            val file = File(previous, databaseName + suffix)
            if (file.exists()) moveFile(file, File(databaseFile.path + suffix))
        }
        previous.deleteRecursively()
        return true
    }

    private fun moveFile(from: File, to: File) {
        if (!from.renameTo(to)) {
            from.copyTo(to, overwrite = true)
            from.delete()
        }
    }
}
