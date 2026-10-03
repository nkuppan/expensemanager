package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.data.repository.backup.BackupCipher
import com.naveenapps.expensemanager.core.data.repository.backup.isSqliteDatabase
import com.naveenapps.expensemanager.core.data.repository.backup.sqliteUserVersion
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.database.PendingDatabaseRestore
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.BackupException
import com.naveenapps.expensemanager.core.repository.BackupException.Reason
import com.naveenapps.expensemanager.core.repository.BackupRepository
import java.io.File
import java.io.IOException
import java.security.GeneralSecurityException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.withContext

/**
 * Replaces de.raphaelebner:roomdatabasebackup, which crashed with "Unable to resume activity":
 * it kept its state (the database, the file name) in the RoomBackup instance and handled the
 * file-picker result on the main thread during onResume. When the Activity was recreated while
 * the picker was open (rotation, theme or locale change, or the process being killed in the
 * background) the new instance received the result with no database set, and `roomDatabase!!`
 * threw. It also closed the shared database and killed the process, even after a plain backup.
 *
 * Here the picker lives in Compose (the result survives recreation), the work runs on IO,
 * a backup never closes the database, and a restore is staged and swapped in at next start.
 */
class BackupRepositoryImpl(
    private val context: Context,
    private val database: ExpenseManagerDatabase,
    private val dispatchers: AppCoroutineDispatchers,
) : BackupRepository {

    override fun backupFileName(): String {
        val stamp = SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.US).format(Date())
        return "expense-manager-$stamp.$BACKUP_EXTENSION"
    }

    override suspend fun backupData(uri: String): Resource<Boolean> = withContext(dispatchers.io) {
        runCatchingBackup {
            val encrypted = BackupCipher.encrypt(snapshotDatabase(), BACKUP_PASSWORD)
            val output = context.contentResolver.openOutputStream(Uri.parse(uri))
                ?: throw BackupException(Reason.IO)
            output.use { it.write(encrypted) }
        }
    }

    override suspend fun restoreData(uri: String): Resource<Boolean> = withContext(dispatchers.io) {
        runCatchingBackup {
            val raw = context.contentResolver.openInputStream(Uri.parse(uri))?.use { it.readBytes() }
                ?: throw BackupException(Reason.IO)
            // Old unencrypted .sqlite3 backups are accepted too.
            val restored = if (raw.isSqliteDatabase()) raw else decrypt(raw)
            if (!restored.isSqliteDatabase()) throw BackupException(Reason.INVALID_FILE)
            // Room can migrate an older schema up, but can't open a newer one.
            if (restored.sqliteUserVersion() > database.openHelper.readableDatabase.version) {
                throw BackupException(Reason.NEWER_VERSION)
            }
            PendingDatabaseRestore.stage(context, databaseName(), restored)
        }
    }

    /**
     * A consistent copy of the database file while Room keeps it open. The checkpoint moves WAL
     * frames into the main file; the (empty) write transaction then blocks writers, and so any
     * further checkpoint, while the file is read.
     */
    private fun snapshotDatabase(): ByteArray {
        val db = database.openHelper.writableDatabase
        db.query("PRAGMA wal_checkpoint(TRUNCATE)").use { it.moveToFirst() }
        val file = File(db.path ?: context.getDatabasePath(databaseName()).path)
        db.beginTransaction()
        try {
            return file.readBytes()
        } finally {
            db.endTransaction()
        }
    }

    private fun decrypt(data: ByteArray): ByteArray = try {
        BackupCipher.decrypt(data, BACKUP_PASSWORD)
    } catch (e: GeneralSecurityException) {
        throw BackupException(Reason.INVALID_FILE, e)
    } catch (e: IllegalArgumentException) {
        throw BackupException(Reason.INVALID_FILE, e)
    }

    private fun databaseName(): String = requireNotNull(database.openHelper.databaseName)

    private inline fun runCatchingBackup(block: () -> Unit): Resource<Boolean> = try {
        block()
        Resource.Success(true)
    } catch (e: CancellationException) {
        throw e
    } catch (e: BackupException) {
        Log.w(TAG, "Backup/restore failed: ${e.reason}", e)
        Resource.Error(e)
    } catch (e: IOException) {
        Log.w(TAG, "Backup/restore I/O failure", e)
        Resource.Error(BackupException(Reason.IO, e))
    } catch (e: Exception) {
        // SecurityException (revoked document permission), provider failures, etc.
        Log.e(TAG, "Backup/restore failed", e)
        Resource.Error(BackupException(Reason.IO, e))
    }

    companion object {
        private const val TAG = "Backup"
        private const val BACKUP_EXTENSION = "sqlite3.aes"

        // Kept as-is so backups made with earlier versions still decrypt. Moving to a per-user
        // passphrase would need a versioned file format; see the note in the PR/changelog.
        private const val BACKUP_PASSWORD = "YOUR_SECRET_PASSWORD"
    }
}
