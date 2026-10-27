package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.core.content.FileProvider
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.data.repository.backup.AutoBackupFiles
import com.naveenapps.expensemanager.core.datastore.AutoBackupDataStore
import com.naveenapps.expensemanager.core.model.AutoBackupError
import com.naveenapps.expensemanager.core.model.AutoBackupFrequency
import com.naveenapps.expensemanager.core.model.AutoBackupResult
import com.naveenapps.expensemanager.core.model.AutoBackupSettings
import com.naveenapps.expensemanager.core.model.BackupFile
import com.naveenapps.expensemanager.core.model.BackupKind
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.AutoBackupRepository
import com.naveenapps.expensemanager.core.repository.BackupRepository
import java.io.File
import java.util.Date
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Writes backups into a Storage Access Framework folder, or the app's own storage until one is
 * picked. The backup bytes come from
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

    override suspend fun backupNow(kind: BackupKind): AutoBackupResult = withContext(dispatchers.io) {
        // The background job and "Back up now" can overlap; one backup at a time.
        lock.withLock { runBackup(kind) }
    }

    private suspend fun runBackup(kind: BackupKind): AutoBackupResult {
        val now = Date()
        val isAuto = kind == BackupKind.AUTO
        val name = if (isAuto) AutoBackupFiles.fileName(now) else AutoBackupFiles.manualFileName(now)
        val folderUri = dataStore.getSettings().first().folderUri
        val written = if (folderUri != null) {
            writeToFolder(Uri.parse(folderUri), name, isAuto)
        } else {
            writeInsideApp(name, isAuto)
        }
        if (written != null) return fail(written)
        dataStore.recordSuccess(now.time, isAuto = isAuto)
        return AutoBackupResult.SUCCESS
    }

    /** Returns the error, or null once the backup is written. */
    private suspend fun writeToFolder(tree: Uri, name: String, isAuto: Boolean): AutoBackupError? {
        if (!hasFolderAccess(tree)) return AutoBackupError.FOLDER_UNAVAILABLE
        val file = try {
            DocumentsContract.createDocument(context.contentResolver, folderDocumentUri(tree), MIME_TYPE, name)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // FileNotFoundException when the folder was deleted, SecurityException when revoked.
            Log.w(TAG, "Couldn't create the backup file", e)
            null
        } ?: return AutoBackupError.FOLDER_UNAVAILABLE

        return when (backupRepository.backupData(file.toString())) {
            is Resource.Success -> {
                if (isAuto) pruneFolder(tree)
                null
            }

            is Resource.Error -> {
                // Don't leave a truncated file that looks like a backup.
                runCatching { DocumentsContract.deleteDocument(context.contentResolver, file) }
                AutoBackupError.WRITE_FAILED
            }
        }
    }

    /** Until a folder is picked, backups live in the app's storage (removed with the app). */
    private suspend fun writeInsideApp(name: String, isAuto: Boolean): AutoBackupError? {
        val directory = insideAppDirectory()
        if (!directory.isDirectory && !directory.mkdirs()) return AutoBackupError.WRITE_FAILED
        val file = File(directory, name)
        return when (backupRepository.backupData(Uri.fromFile(file).toString())) {
            is Resource.Success -> {
                if (isAuto) {
                    val names = directory.list()?.toList().orEmpty()
                    AutoBackupFiles.toDelete(names, AutoBackupRepository.KEEP_LAST_BACKUPS)
                        .forEach { File(directory, it).delete() }
                }
                null
            }

            is Resource.Error -> {
                file.delete()
                AutoBackupError.WRITE_FAILED
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

    override suspend fun getBackups(): List<BackupFile> = withContext(dispatchers.io) {
        val insideApp = insideAppDirectory().listFiles().orEmpty().filter { it.isFile }.mapNotNull { file ->
            val parsed = AutoBackupFiles.parse(file.name) ?: return@mapNotNull null
            BackupFile(
                uri = Uri.fromFile(file).toString(),
                name = file.name,
                createdAt = parsed.createdAt,
                sizeBytes = file.length(),
                kind = if (parsed.isAuto) BackupKind.AUTO else BackupKind.MANUAL,
                isInsideApp = true,
            )
        }
        val inFolder = dataStore.getSettings().first().folderUri
            ?.let { Uri.parse(it) }
            ?.takeIf { hasFolderAccess(it) }
            ?.let { tree ->
                listFolder(tree).mapNotNull { document ->
                    val parsed = AutoBackupFiles.parse(document.name) ?: return@mapNotNull null
                    BackupFile(
                        uri = DocumentsContract.buildDocumentUriUsingTree(tree, document.id).toString(),
                        name = document.name,
                        createdAt = parsed.createdAt,
                        sizeBytes = document.size,
                        kind = if (parsed.isAuto) BackupKind.AUTO else BackupKind.MANUAL,
                        isInsideApp = false,
                    )
                }
            }
            .orEmpty()
        (inFolder + insideApp).sortedByDescending { it.createdAt }
    }

    override suspend fun prepareShare(backup: BackupFile, passphrase: String?): String? = withContext(dispatchers.io) {
        try {
            // Only the latest shared copy is kept around.
            val directory = File(context.cacheDir, SHARE_DIRECTORY)
            directory.deleteRecursively()
            if (!directory.mkdirs()) return@withContext null
            val copy = File(directory, backup.name)
            if (passphrase.isNullOrEmpty()) {
                val input = context.contentResolver.openInputStream(Uri.parse(backup.uri)) ?: return@withContext null
                input.use { source -> copy.outputStream().use { source.copyTo(it) } }
            } else {
                val result = backupRepository.protectBackup(backup.uri, Uri.fromFile(copy).toString(), passphrase)
                if (result is Resource.Error) return@withContext null
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", copy).toString()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Couldn't prepare the backup to share", e)
            null
        }
    }

    private fun insideAppDirectory(): File = File(context.noBackupFilesDir, INSIDE_APP_DIRECTORY)

    private class Document(val id: String, val name: String, val size: Long)

    private fun listFolder(tree: Uri): List<Document> {
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree))
        val documents = mutableListOf<Document>()
        try {
            context.contentResolver.query(
                children,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_SIZE,
                ),
                null,
                null,
                null,
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getString(0) ?: continue
                    val name = cursor.getString(1) ?: continue
                    val size = if (cursor.isNull(2)) 0L else cursor.getLong(2)
                    documents += Document(id, name, size)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Couldn't list the backup folder", e)
        }
        return documents
    }

    /** Keeps the newest automatic backups. A failed delete is retried on the next run. */
    private fun pruneFolder(tree: Uri) {
        val documents = listFolder(tree).associate { it.name to it.id }
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

        // In noBackupFilesDir: Android's own cloud backup must not copy whole backups around.
        private const val INSIDE_APP_DIRECTORY = "backups"

        // Matches the "shared_backups" cache-path in the app's FileProvider paths.
        private const val SHARE_DIRECTORY = "shared_backups"

        // Same as the manual backup picker, so providers treat both files alike.
        private const val MIME_TYPE = "application/octet-stream"
        private const val FOLDER_ACCESS = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        private val lock = Mutex()
    }
}
