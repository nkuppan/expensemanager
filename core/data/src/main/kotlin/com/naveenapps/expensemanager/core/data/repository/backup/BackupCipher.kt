package com.naveenapps.expensemanager.core.data.repository.backup

import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-GCM with a PBKDF2 key, byte-compatible with files written by
 * de.raphaelebner:roomdatabasebackup (`customEncryptPassword`), so backups users already made
 * still restore. Layout: [int nonceSize][nonce][ciphertext + 128-bit tag].
 */
internal object BackupCipher {

    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val NONCE_SIZE = 12
    private const val TAG_BITS = 128
    private const val KEY_ITERATIONS = 65536
    private const val KEY_BITS = 128

    fun encrypt(plain: ByteArray, password: String): ByteArray {
        val nonce = ByteArray(NONCE_SIZE).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key(password, nonce), GCMParameterSpec(TAG_BITS, nonce))
        val encrypted = cipher.doFinal(plain)
        return ByteBuffer.allocate(4 + nonce.size + encrypted.size)
            .putInt(nonce.size)
            .put(nonce)
            .put(encrypted)
            .array()
    }

    /** Throws (IllegalArgumentException or a GeneralSecurityException) if [data] isn't ours. */
    fun decrypt(data: ByteArray, password: String): ByteArray {
        val buffer = ByteBuffer.wrap(data)
        require(buffer.remaining() > 4) { "Backup is too short" }
        val nonceSize = buffer.int
        require(nonceSize in 12..15 && buffer.remaining() > nonceSize) { "Bad nonce size" }
        val nonce = ByteArray(nonceSize).also { buffer.get(it) }
        val encrypted = ByteArray(buffer.remaining()).also { buffer.get(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key(password, nonce), GCMParameterSpec(TAG_BITS, nonce))
        return cipher.doFinal(encrypted)
    }

    private fun key(password: String, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(password.toCharArray(), salt, KEY_ITERATIONS, KEY_BITS)
        val encoded = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
        return SecretKeySpec(encoded, "AES")
    }
}

private val SQLITE_HEADER = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)

internal fun ByteArray.isSqliteDatabase(): Boolean = size >= 100 && SQLITE_HEADER.indices.all { this[it] == SQLITE_HEADER[it] }

/** PRAGMA user_version, which Room uses as the schema version (header offset 60, big-endian). */
internal fun ByteArray.sqliteUserVersion(): Int = ByteBuffer.wrap(this, 60, 4).int
