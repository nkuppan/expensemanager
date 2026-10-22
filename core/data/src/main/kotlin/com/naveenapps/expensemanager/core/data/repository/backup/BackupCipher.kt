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

    // region User passphrase (format 2)

    /**
     * Backups protected with a passphrase the user chose. The built-in password above ships in
     * the APK, so it only obfuscates; this one is real encryption. A different, stronger key
     * derivation and its own random salt, behind a magic prefix that can't collide with the
     * legacy layout (which starts with the nonce size, 0x0000000C).
     *
     * Layout: [magic "EMB2"][salt 16][int nonceSize][nonce][ciphertext + 128-bit tag].
     */
    private val PASSPHRASE_MAGIC = "EMB2".toByteArray(Charsets.US_ASCII)
    private const val PASSPHRASE_SALT_SIZE = 16
    private const val PASSPHRASE_ITERATIONS = 120_000
    private const val PASSPHRASE_KEY_BITS = 256

    fun isPassphraseProtected(data: ByteArray): Boolean = data.size > PASSPHRASE_MAGIC.size && PASSPHRASE_MAGIC.indices.all { data[it] == PASSPHRASE_MAGIC[it] }

    fun encryptWithPassphrase(plain: ByteArray, passphrase: String): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(PASSPHRASE_SALT_SIZE).also { random.nextBytes(it) }
        val nonce = ByteArray(NONCE_SIZE).also { random.nextBytes(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, passphraseKey(passphrase, salt), GCMParameterSpec(TAG_BITS, nonce))
        val encrypted = cipher.doFinal(plain)
        return ByteBuffer.allocate(PASSPHRASE_MAGIC.size + salt.size + 4 + nonce.size + encrypted.size)
            .put(PASSPHRASE_MAGIC)
            .put(salt)
            .putInt(nonce.size)
            .put(nonce)
            .put(encrypted)
            .array()
    }

    /** Throws a GeneralSecurityException for a wrong passphrase or a tampered file. */
    fun decryptWithPassphrase(data: ByteArray, passphrase: String): ByteArray {
        require(isPassphraseProtected(data)) { "Not a passphrase-protected backup" }
        val buffer = ByteBuffer.wrap(data, PASSPHRASE_MAGIC.size, data.size - PASSPHRASE_MAGIC.size)
        require(buffer.remaining() > PASSPHRASE_SALT_SIZE + 4) { "Backup is too short" }
        val salt = ByteArray(PASSPHRASE_SALT_SIZE).also { buffer.get(it) }
        val nonceSize = buffer.int
        require(nonceSize in 12..15 && buffer.remaining() > nonceSize) { "Bad nonce size" }
        val nonce = ByteArray(nonceSize).also { buffer.get(it) }
        val encrypted = ByteArray(buffer.remaining()).also { buffer.get(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, passphraseKey(passphrase, salt), GCMParameterSpec(TAG_BITS, nonce))
        return cipher.doFinal(encrypted)
    }

    private fun passphraseKey(passphrase: String, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, PASSPHRASE_ITERATIONS, PASSPHRASE_KEY_BITS)
        val encoded = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        spec.clearPassword()
        return SecretKeySpec(encoded, "AES")
    }

    // endregion

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
