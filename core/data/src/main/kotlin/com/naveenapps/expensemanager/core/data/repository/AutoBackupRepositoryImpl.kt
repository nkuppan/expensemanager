package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.data.repository.backup.AutoBackupFiles
import com.naveenapps.expensemanager.core.datastore.AutoBackupDataStore
import com.naveenapps.expensemanager.core.model.AutoBackupError
import com.naveenapps.expensemanager.core.model.AutoBackupFrequency
import com.naveenapps.expensemanager.core.model.AutoBackupResult
import com.naveenapps.expensemanager.core.model.AutoBackupSettings
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.AutoBackupRepository
import com.naveenapps.expensemanager.core.repository.BackupRepository
import java.util.Date
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Writes automatic backups into a Storage Access Framework folder. The backup bytes come from
 * [BackupRepository], so automatic and manual backups are the same format and Restore reads
 * either. Without a user passphrase (none can be asked for in the background) they use the
 * app's built-in key, like a manual backup made without one.
 */
class AutoBackupRepositoryImpl(
    private val context: Context,
    private val dataStore: AutoBackupDataStore,
    private val backupRepository: BackupRepository,
    private val dispatchers: AppCoroutineDispatchers,
) : AutoBackupRepository {

    override fun getSettings(): Flow<AutoBackupSettings> = dataStore.getSettings()

    override suspend fun setEnabled(enabled: Boolean) {
        dataStore.setEnabled(enabled)
    }

    override suspend fun setFrequency(frequency: AutoBackupFrequency) {
        dataStore.setFrequency(frequency)
    }

    override suspend fun setFolder(treeUri: String): Boolean = withContext(dispatchers.io) {
        val uri = Uri.parse(treeUri)
        try {
            context.contentResolver.takePersistableUriPermission(uri, FOLDER_ACCESS)
        } catch (e: SecurityException) {
            Log.w(TAG, "No lasting access to the chosen folder", e)
            return@withContext false
        }
        val previous = dataStore.getSettings().first().folderUri
        if (previous != null && previous != treeUri) {
            // Apps can only hold a limited number of persisted grants; don't leak the old one.
            runCatching { context.contentResolver.releasePersistableUriPermission(Uri.parse(previous), FOLDER_ACCESS) }
        }
        dataStore.setFolder(treeUri, folderDisplayName(uri))
        true
    }

    override suspend fun snoozeFailureWarning(untilMillis: Long) {
        dataStore.snoozeWarning(untilMillis)
    }

    override suspend fun markFailureNotified() {
        dataStore.setFailureNotified()
    }

    override suspend fun backupNow(): AutoBackupResult = withContext(dispatchers.io) {
        // The background job and "Back up now" can overlap; one backup at a time.
        lock.withLock { runBackup() }
    }

    private suspend fun runBackup(): AutoBackupResult {
        val folderUri = dataStore.getSettings().first().folderUri ?: return AutoBackupResult.SKIPPED
        val tree = Uri.parse(folderUri)
        if (!hasFolderAccess(tree)) return fail(AutoBackupError.FOLDER_UNAVAILABLE)

        val now = Date()
        val file = try {
            DocumentsContract.createDocument(
                context.contentResolver,
                folderDocumentUri(tree),
                MIME_TYPE,
                AutoBackupFiles.fileName(now),
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // FileNotFoundException when the folder was deleted, SecurityException when revoked.
            Log.w(TAG, "Couldn't create the backup file", e)
            null
        } ?: return fail(AutoBackupError.FOLDER_UNAVAILABLE)

        return when (backupRepository.backupData(file.toString())) {
            is Resource.Success -> {
                pruneOldBackups(tree)
                dataStore.recordSuccess(now.time)
                AutoBackupResult.SUCCESS
            }

            is Resource.Error -> {
                // Don't leave a truncated file that looks like a backup.
                runCatching { DocumentsContract.deleteDocument(context.contentResolver, file) }
                fail(AutoBackupError.WRITE_FAILED)
            }
        }
    }

    private suspend fun fail(error: AutoBackupError): AutoBackupResult {
        dataStore.recordError(error)
        return when (error) {
            AutoBackupError.FOLDER_UNAVAILABLE -> AutoBackupResult.FOLDER_UNAVAILABLE
            AutoBackupError.WRITE_FAILED -> AutoBackupResult.FAILED
        }
    }

    /** Keeps the newest automatic backups. A failed delete is retried on the next run. */
    private fun pruneOldBackups(tree: Uri) {
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree))
        val documents = mutableMapOf<String, String>() // display name -> document id
        try {
            context.contentResolver.query(
                children,
                arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                null,
                null,
                null,
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getString(0) ?: continue
                    val name = cursor.getString(1) ?: continue
                    documents[name] = id
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Couldn't list the backup folder", e)
            return
        }
        AutoBackupFiles.toDelete(documents.keys.toList(), AutoBackupRepository.KEEP_LAST_BACKUPS).forEach { name ->
            val id = documents.getValue(name)
            runCatching {
                DocumentsContract.deleteDocument(context.contentResolver, DocumentsContract.buildDocumentUriUsingTree(tree, id))
            }.onFailure { Log.w(TAG, "Couldn't delete old backup $name", it) }
        }
    }

    private fun hasFolderAccess(tree: Uri): Boolean = context.contentResolver.persistedUriPermissions.any {
        it.uri == tree && it.isReadPermission && it.isWritePermission
    }

    private fun folderDocumentUri(tree: Uri): Uri = DocumentsContract.buildDocumentUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree))

    private fun folderDisplayName(tree: Uri): String? = runCatching {
        context.contentResolver.query(
            folderDocumentUri(tree),
            arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
    }.getOrNull()

    companion object {
        private const val TAG = "AutoBackup"

        // Same as the manual backup picker, so providers treat both files alike.
        private const val MIME_TYPE = "application/octet-stream"
        private const val FOLDER_ACCESS = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        private val lock = Mutex()
    }
}
