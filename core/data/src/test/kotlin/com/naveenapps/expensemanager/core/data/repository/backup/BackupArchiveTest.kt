package com.naveenapps.expensemanager.core.data.repository.backup

import com.google.common.truth.Truth.assertThat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.security.GeneralSecurityException
import java.security.Security
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.BeforeClass
import org.junit.Test

class BackupArchiveTest {

    private val database = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII) + ByteArray(200) { 1 }

    private fun tempDir(): File = Files.createTempDirectory("backup-test").toFile()

    @Test
    fun imagesAndDatabaseRoundTrip() {
        val source = tempDir()
        File(source, "transaction_attachments").apply { mkdirs() }.resolve("a.jpg").writeBytes(byteArrayOf(1, 2))
        File(source, "category_images").apply { mkdirs() }.resolve("b.jpg").writeBytes(byteArrayOf(3))
        File(source, "unrelated").apply { mkdirs() }.resolve("c.txt").writeBytes(byteArrayOf(4))

        val archive = BackupArchive.write(database, source)
        assertThat(BackupArchive.isArchive(archive)).isTrue()

        val contents = BackupArchive.read(archive)
        assertThat(contents.database).isEqualTo(database)
        assertThat(contents.files.keys).containsExactly("transaction_attachments/a.jpg", "category_images/b.jpg")

        val target = tempDir()
        BackupArchive.extractFiles(contents.files, target)
        assertThat(File(target, "transaction_attachments/a.jpg").readBytes()).isEqualTo(byteArrayOf(1, 2))
        assertThat(File(target, "unrelated").exists()).isFalse()
    }

    @Test(expected = IOException::class)
    fun pathTraversalEntriesAreRejected() {
        val bytes = ByteArrayOutputStream().also { output ->
            ZipOutputStream(output).use { zip ->
                zip.putNextEntry(ZipEntry(BackupArchive.DATABASE_ENTRY))
                zip.write(database)
                zip.closeEntry()
                zip.putNextEntry(ZipEntry("files/transaction_attachments/../../shared_prefs/x.xml"))
                zip.write(byteArrayOf(1))
                zip.closeEntry()
            }
        }.toByteArray()

        BackupArchive.read(bytes)
    }

    @Test(expected = IOException::class)
    fun archiveWithoutDatabaseIsRejected() {
        val bytes = ByteArrayOutputStream().also { output ->
            ZipOutputStream(output).use { zip ->
                zip.putNextEntry(ZipEntry("files/category_images/a.jpg"))
                zip.write(byteArrayOf(1))
                zip.closeEntry()
            }
        }.toByteArray()

        BackupArchive.read(bytes)
    }

    @Test
    fun passphraseBackupRoundTripsAndIsDistinguishable() {
        val encrypted = BackupCipher.encryptWithPassphrase(database, "correct horse")

        assertThat(BackupCipher.isPassphraseProtected(encrypted)).isTrue()
        assertThat(BackupCipher.isPassphraseProtected(BackupCipher.encrypt(database, "legacy"))).isFalse()
        assertThat(BackupCipher.decryptWithPassphrase(encrypted, "correct horse")).isEqualTo(database)
    }

    @Test(expected = GeneralSecurityException::class)
    fun wrongPassphraseIsRejected() {
        val encrypted = BackupCipher.encryptWithPassphrase(database, "correct horse")
        BackupCipher.decryptWithPassphrase(encrypted, "battery staple")
    }

    companion object {
        /** Same Conscrypt workaround as BackupCipherTest: these are plain JVM cipher tests. */
        @BeforeClass
        @JvmStatic
        fun useJdkSecurityProviders() {
            Security.getProviders()
                .filter { it.javaClass.name.startsWith("org.conscrypt.") }
                .forEach { Security.removeProvider(it.name) }
        }
    }
}
