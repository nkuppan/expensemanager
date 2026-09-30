package com.naveenapps.expensemanager.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.KeyStore

/**
 * Instrumented because the Android Keystore has no JVM implementation: the
 * cipher round trip and the unreadable-value paths can only be exercised on a
 * device.
 *
 * One DataStore serves the whole class - DataStore refuses two active
 * instances on the same file - so tests start from a cleared value instead of
 * a fresh store.
 */
@RunWith(AndroidJUnit4::class)
class StatementPasswordStoreTest {

    private val password = "mock-statement-password"

    @Before
    fun resetState() {
        runBlocking { store.clear() }
        deleteKey()
    }

    @After
    fun cleanUp() {
        runBlocking { runCatching { store.clear() } }
        dataStoreFile.delete()
        deleteKey()
    }

    @Test
    fun read_returnsNothingWhenNoPasswordIsStored() = runBlocking {
        assertThat(store.read()).isNull()
    }

    @Test
    fun writeThenRead_returnsThePassword() = runBlocking {
        store.write(password)

        assertThat(store.read()).isEqualTo(password)
    }

    @Test
    fun write_isVisibleToAnotherStoreInstance() = runBlocking {
        store.write(password)

        assertThat(StatementPasswordStore(dataStore).read()).isEqualTo(password)
    }

    @Test
    fun write_doesNotStoreThePasswordInTheClear() = runBlocking {
        store.write(password)

        val raw = dataStoreFile.readBytes().toString(Charsets.ISO_8859_1)
        assertThat(raw).contains(ENCRYPTED_KEY_NAME)
        assertThat(raw).doesNotContain(password)
    }

    @Test
    fun write_usesAFreshIvSoTheCiphertextDiffersEachTime() = runBlocking {
        store.write(password)
        val first = dataStoreFile.readBytes().toString(Charsets.ISO_8859_1)

        store.write(password)
        val second = dataStoreFile.readBytes().toString(Charsets.ISO_8859_1)

        assertThat(second).isNotEqualTo(first)
        assertThat(store.read()).isEqualTo(password)
    }

    @Test
    fun write_overwritesAPreviousPassword() = runBlocking {
        store.write(password)

        store.write("another-mock-password")

        assertThat(store.read()).isEqualTo("another-mock-password")
    }

    @Test
    fun clear_removesTheStoredPassword() = runBlocking {
        store.write(password)

        store.clear()

        assertThat(store.read()).isNull()
        // DataStore deletes the file outright once the last key is removed.
        val raw = if (dataStoreFile.exists()) {
            dataStoreFile.readBytes().toString(Charsets.ISO_8859_1)
        } else {
            ""
        }
        assertThat(raw).doesNotContain(ENCRYPTED_KEY_NAME)
    }

    @Test
    fun write_blankValueClearsTheStoredPassword() = runBlocking {
        store.write(password)

        store.write("   ")

        assertThat(store.read()).isNull()
    }

    @Test
    fun read_returnsNullForAMalformedStoredValue() = runBlocking {
        dataStore.edit { it[stringPreferencesKey(ENCRYPTED_KEY_NAME)] = "bm90LWEtY2lwaGVy" }

        assertThat(store.read()).isNull()
    }

    @Test
    fun read_returnsNullWhenTheGcmTagDoesNotMatch() = runBlocking {
        // Well-formed base64 in the iv:ciphertext shape, but not encrypted with
        // the stored key, so the GCM tag check has to reject it.
        dataStore.edit {
            it[stringPreferencesKey(ENCRYPTED_KEY_NAME)] =
                "AAAAAAAAAAAAAAAAAAAAAA:BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"
        }

        assertThat(store.read()).isNull()
    }

    @Test
    fun read_returnsNullWhenTheKeystoreKeyIsDeleted() = runBlocking {
        store.write(password)
        // Stands in for a restored backup or a key the system has invalidated.
        deleteKey()

        assertThat(store.read()).isNull()
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "expensemanager_statement_pdf_password"
        private const val ENCRYPTED_KEY_NAME = "statement_pdf_password_encrypted"
        private const val TEST_FILE = "statement_password_store_test.preferences_pb"

        private lateinit var dataStoreFile: File
        private lateinit var dataStore: DataStore<Preferences>
        private lateinit var store: StatementPasswordStore

        @BeforeClass
        @JvmStatic
        fun createStore() {
            val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
            dataStoreFile = File(context.filesDir, TEST_FILE)
            dataStoreFile.delete()
            dataStore = PreferenceDataStoreFactory.create { dataStoreFile }
            store = StatementPasswordStore(dataStore)
        }

        private fun deleteKey() {
            runCatching {
                KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }.deleteEntry(KEY_ALIAS)
            }
        }
    }
}
