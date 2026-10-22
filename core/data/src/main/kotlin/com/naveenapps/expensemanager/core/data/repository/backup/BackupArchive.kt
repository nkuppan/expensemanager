package com.naveenapps.expensemanager.core.data.repository.backup

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * The (unencrypted) contents of a backup since format 2: a zip holding the database and the
 * app's image folders. Format 1 backups were the bare database file, so category/account photos
 * and transaction attachments were lost when restoring on a new phone.
 */
internal object BackupArchive {

    const val DATABASE_ENTRY = "database.sqlite3"
    private const val FILES_PREFIX = "files/"

    /** Folders under filesDir that hold user images (see ImageStorageRepositoryImpl). */
    val IMAGE_DIRECTORIES = listOf("category_images", "account_images", "transaction_attachments")

    private val ZIP_HEADER = byteArrayOf(0x50, 0x4B, 0x03, 0x04)

    class Contents(val database: ByteArray, val files: Map<String, ByteArray>)

    fun isArchive(bytes: ByteArray): Boolean = bytes.size > ZIP_HEADER.size && ZIP_HEADER.indices.all { bytes[it] == ZIP_HEADER[it] }

    fun write(database: ByteArray, filesDir: File): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry(DATABASE_ENTRY))
            zip.write(database)
            zip.closeEntry()
            IMAGE_DIRECTORIES.forEach { directory ->
                File(filesDir, directory).listFiles()?.filter { it.isFile }?.forEach { file ->
                    zip.putNextEntry(ZipEntry("$FILES_PREFIX$directory/${file.name}"))
                    file.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }
        }
        return output.toByteArray()
    }

    /** Reads and validates an archive; throws IOException if it isn't one of ours. */
    fun read(bytes: ByteArray): Contents {
        var database: ByteArray? = null
        val files = mutableMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                when {
                    entry.isDirectory -> Unit
                    entry.name == DATABASE_ENTRY -> database = zip.readBytes()
                    else -> files[safeRelativePath(entry.name)] = zip.readBytes()
                }
            }
        }
        return Contents(database ?: throw IOException("Backup has no database"), files)
    }

    /** Writes [files] (as returned by [read]) under [filesDir], keeping existing files. */
    fun extractFiles(files: Map<String, ByteArray>, filesDir: File) {
        files.forEach { (relativePath, data) ->
            val target = File(filesDir, relativePath)
            target.parentFile?.mkdirs()
            target.writeBytes(data)
        }
    }

    /**
     * Only "files/<image folder>/<plain file name>" is accepted, which also stops a crafted
     * archive from writing outside the app's folders ("zip slip": names with ../ or a path).
     */
    private fun safeRelativePath(name: String): String {
        val parts = name.removePrefix(FILES_PREFIX).split('/')
        val valid = name.startsWith(FILES_PREFIX) &&
            parts.size == 2 &&
            parts[0] in IMAGE_DIRECTORIES &&
            parts[1].isNotBlank() &&
            parts[1] != "." &&
            parts[1] != ".." &&
            '\\' !in parts[1]
        if (!valid) throw IOException("Unexpected entry in backup: $name")
        return "${parts[0]}/${parts[1]}"
    }
}
