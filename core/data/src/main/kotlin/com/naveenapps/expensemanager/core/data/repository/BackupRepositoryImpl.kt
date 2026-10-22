package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.data.repository.backup.BackupArchive
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

    override suspend fun backupData(uri: String, passphrase: String?): Resource<Boolean> = withContext(dispatchers.io) {
        runCatchingBackup {
            val archive = BackupArchive.write(snapshotDatabase(), context.filesDir)
            val encrypted = if (passphrase.isNullOrEmpty()) {
                BackupCipher.encrypt(archive, BACKUP_PASSWORD)
            } else {
                BackupCipher.encryptWithPassphrase(archive, passphrase)
            }
            val output = context.contentResolver.openOutputStream(Uri.parse(uri))
                ?: throw BackupException(Reason.IO)
            output.use { it.write(encrypted) }
        }
    }

    override suspend fun restoreData(uri: String, passphrase: String?): Resource<Boolean> = withContext(dispatchers.io) {
        runCatchingBackup {
            val raw = context.contentResolver.openInputStream(Uri.parse(uri))?.use { it.readBytes() }
                ?: throw BackupException(Reason.IO)
            // Old unencrypted .sqlite3 backups are accepted too.
            val payload = when {
                raw.isSqliteDatabase() -> raw
                BackupCipher.isPassphraseProtected(raw) -> decryptWithPassphrase(raw, passphrase)
                else -> decrypt(raw)
            }
            // Format 2 is a zip of the database plus images; format 1 was the bare database.
            val contents = if (BackupArchive.isArchive(payload)) {
                try {
                    BackupArchive.read(payload)
                } catch (e: IOException) {
                    throw BackupException(Reason.INVALID_FILE, e)
                }
            } else {
                BackupArchive.Contents(payload, emptyMap())
            }
            val restored = contents.database
            if (!restored.isSqliteDatabase()) throw BackupException(Reason.INVALID_FILE)
            // Room can migrate an older schema up, but can't open a newer one, nor one older than
            // the oldest registered migration.
            val version = restored.sqliteUserVersion()
            if (version > database.openHelper.readableDatabase.version) {
                throw BackupException(Reason.NEWER_VERSION)
            }
            if (version < PendingDatabaseRestore.OLDEST_RESTORABLE_VERSION) {
                throw BackupException(Reason.INVALID_FILE)
            }
            // Any SQLite file passes the header check; make sure it's actually ours.
            if (PendingDatabaseRestore.missingRequiredTables(context, restored).isNotEmpty()) {
                throw BackupException(Reason.INVALID_FILE)
            }
            PendingDatabaseRestore.stage(context, databaseName(), restored)
            // Images get new random names when saved, so restoring them alongside the
            // current ones can't overwrite anything the user still has.
            BackupArchive.extractFiles(contents.files, context.filesDir)
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

    private fun decryptWithPassphrase(data: ByteArray, passphrase: String?): ByteArray {
        if (passphrase.isNullOrEmpty()) throw BackupException(Reason.PASSPHRASE_REQUIRED)
        return try {
            BackupCipher.decryptWithPassphrase(data, passphrase)
        } catch (e: GeneralSecurityException) {
            throw BackupException(Reason.WRONG_PASSPHRASE, e)
        } catch (e: IllegalArgumentException) {
            throw BackupException(Reason.INVALID_FILE, e)
        }
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

        // Kept as-is so backups made with earlier versions still decrypt. It ships in the APK,
        // so it only obfuscates; users who want real encryption set a passphrase (format 2).
        private const val BACKUP_PASSWORD = "YOUR_SECRET_PASSWORD"
    }
}
