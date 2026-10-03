package com.naveenapps.expensemanager.core.data.repository.backup

import java.nio.ByteBuffer
import java.security.GeneralSecurityException
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCipherTest {

    private val database = ByteArray(4096).also { bytes ->
        "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII).copyInto(bytes)
        ByteBuffer.wrap(bytes, 60, 4).putInt(8)
        for (i in 100 until bytes.size) bytes[i] = (i % 251).toByte()
    }

    @Test
    fun roundTripRestoresTheSameBytes() {
        val encrypted = BackupCipher.encrypt(database, PASSWORD)

        assertFalse(encrypted.isSqliteDatabase())
        assertEquals(12, ByteBuffer.wrap(encrypted).int)
        assertArrayEquals(database, BackupCipher.decrypt(encrypted, PASSWORD))
    }

    @Test(expected = GeneralSecurityException::class)
    fun wrongPasswordIsRejected() {
        BackupCipher.decrypt(BackupCipher.encrypt(database, PASSWORD), "other")
    }

    @Test(expected = GeneralSecurityException::class)
    fun tamperedFileIsRejected() {
        val encrypted = BackupCipher.encrypt(database, PASSWORD)
        encrypted[encrypted.size - 1] = (encrypted.last() + 1).toByte()
        BackupCipher.decrypt(encrypted, PASSWORD)
    }

    @Test(expected = IllegalArgumentException::class)
    fun randomFileIsRejected() {
        BackupCipher.decrypt("not a backup at all".toByteArray(), PASSWORD)
    }

    @Test
    fun readsSqliteHeaderAndSchemaVersion() {
        assertTrue(database.isSqliteDatabase())
        assertEquals(8, database.sqliteUserVersion())
        assertFalse(ByteArray(200).isSqliteDatabase())
    }

    private companion object {
        const val PASSWORD = "YOUR_SECRET_PASSWORD"
    }
}
