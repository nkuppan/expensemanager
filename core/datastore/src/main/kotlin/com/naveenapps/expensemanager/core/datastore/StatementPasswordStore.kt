package com.naveenapps.expensemanager.core.datastore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Remembers the password of a password-protected statement PDF.
 *
 * The password is a bank credential, so it never reaches the preferences file in
 * the clear: an AES-GCM key generated inside the Android Keystore encrypts it
 * and only the ciphertext is stored. The key is non-exportable and never leaves
 * the device, which makes the stored value useless anywhere else. The
 * preferences file also sits in the `file` domain, which the app excludes from
 * cloud backup and device transfer.
 *
 * Failures are deliberately swallowed. A password that cannot be read back (for
 * example after the Keystore key was invalidated) is reported as "no password
 * stored", and a password that cannot be written is simply not remembered -
 * neither is worth failing an import over.
 */
class StatementPasswordStore(private val dataStore: DataStore<Preferences>) {

    private val encryptedPasswordKey = stringPreferencesKey("statement_pdf_password_encrypted")

    /** The remembered password, or null when none is stored or it is unreadable. */
    suspend fun read(): String? {
        val stored = runCatching { dataStore.data.first()[encryptedPasswordKey] }.getOrNull()
            ?: return null
        return runCatching { decrypt(stored) }.getOrNull()
    }

    /** Stores [password] encrypted, or clears the stored value when null/blank. */
    suspend fun write(password: String?) {
        if (password.isNullOrBlank()) {
            clear()
            return
        }
        val encrypted = runCatching { encrypt(password) }.getOrNull() ?: return
        runCatching { dataStore.edit { it[encryptedPasswordKey] = encrypted } }
    }

    suspend fun clear() {
        runCatching { dataStore.edit { it.remove(encryptedPasswordKey) } }
    }

    private fun encrypt(password: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val cipherText = cipher.doFinal(password.toByteArray(Charsets.UTF_8))
        // GCM needs a fresh IV per encryption; it is stored alongside the
        // ciphertext because it is not secret.
        return listOf(
            Base64.encodeToString(cipher.iv, Base64.NO_WRAP),
            Base64.encodeToString(cipherText, Base64.NO_WRAP),
        ).joinToString(SEPARATOR)
    }

    private fun decrypt(stored: String): String? {
        val parts = stored.split(SEPARATOR)
        if (parts.size != 2) return null
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val cipherText = Base64.decode(parts[1], Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKey(),
            GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv),
        )
        return String(cipher.doFinal(cipherText), Charsets.UTF_8)
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existing != null) return existing.secretKey

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE_BITS)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "expensemanager_statement_pdf_password"
        const val KEY_SIZE_BITS = 256
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH_BITS = 128
        const val SEPARATOR = ":"
    }
}
