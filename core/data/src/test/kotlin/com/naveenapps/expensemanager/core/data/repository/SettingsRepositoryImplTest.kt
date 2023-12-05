package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.datastore.SettingsDataStore
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsRepositoryImplTest : BaseCoroutineTest() {

    private val testContext: Context = ApplicationProvider.getApplicationContext()
    private val testCoroutineScope = TestScope(testCoroutineDispatcher.dispatcher)
    private val testDataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = testCoroutineScope,
            produceFile = {
                testContext.preferencesDataStoreFile("TEST_DATASTORE_NAME")
            },
        )

    private val repository: SettingsRepository = SettingsRepositoryImpl(
        SettingsDataStore(testDataStore),
        AppCoroutineDispatchers(
            testCoroutineDispatcher.dispatcher,
            testCoroutineDispatcher.dispatcher,
            testCoroutineDispatcher.dispatcher,
        ),
    )

    @Test
    fun `when getTransactionTypes for first time should return null`() = runTest {
        repository.getTransactionTypes().test {
            val item = awaitItem()
            Truth.assertThat(item).isNotNull()
            Truth.assertThat(item).isEmpty()
        }
    }

    @Test
    fun `when setTransactionTypes with account id should return the success`() = runTest {
        val transactionTypes =
            listOf(TransactionType.EXPENSE, TransactionType.INCOME, TransactionType.TRANSFER)
        val response = repository.setTransactionTypes(transactionTypes)
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        Truth.assertThat((response as Resource.Success).data).isTrue()
    }

    @Test
    fun `when getTransactionTypes after saving the item should return the saved value`() = runTest {

        val transactionTypes =
            listOf(TransactionType.EXPENSE, TransactionType.INCOME, TransactionType.TRANSFER)

        repository.setTransactionTypes(transactionTypes)

        repository.getTransactionTypes().test {
            val item = awaitItem()
            Truth.assertThat(item).isNotNull()
            Truth.assertThat(item).isNotEmpty()
            Truth.assertThat(item).isEqualTo(transactionTypes)
        }
    }

    @Test
    fun `when getAccounts for first time should return null`() = runTest {
        repository.getAccounts().test {
            val item = awaitItem()
            Truth.assertThat(item).isNotNull()
            Truth.assertThat(item).isEmpty()
        }
    }

    @Test
    fun `when setAccounts with account id should return the success`() = runTest {
        val accounts = listOf("sampleId")
        val response = repository.setAccounts(accounts)
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        Truth.assertThat((response as Resource.Success).data).isTrue()
    }

    @Test
    fun `when getAccounts after saving the item should return the saved value`() = runTest {
        val accounts = listOf("sampleId")
        repository.setAccounts(accounts)
        repository.getAccounts().test {
            val item = awaitItem()
            Truth.assertThat(item).isNotNull()
            Truth.assertThat(item).isNotEmpty()
            Truth.assertThat(item).isEqualTo(accounts)
        }
    }

    @Test
    fun `when getCategories for first time should return null`() = runTest {
        repository.getCategories().test {
            val item = awaitItem()
            Truth.assertThat(item).isNotNull()
            Truth.assertThat(item).isEmpty()
        }
    }

    @Test
    fun `when setCategories with account id should return the success`() = runTest {
        val categories = listOf("sampleId")
        val response = repository.setCategories(categories)
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        Truth.assertThat((response as Resource.Success).data).isTrue()
    }

    @Test
    fun `when getCategories after saving the item should return the saved value`() = runTest {
        val categories = listOf("sampleId")
        repository.setCategories(categories)
        repository.getCategories().test {
            val item = awaitItem()
            Truth.assertThat(item).isNotNull()
            Truth.assertThat(item).isNotEmpty()
            Truth.assertThat(item).isEqualTo(categories)
        }
    }

    @Test
    fun `when isPreloaded for first time should return false`() = runTest {
        repository.isPreloaded().test {
            val item = awaitItem()
            Truth.assertThat(item).isNotNull()
            Truth.assertThat(item).isFalse()
        }
    }

    @Test
    fun `when isPreloaded after saving the item should return the saved value`() = runTest {
        repository.setPreloaded(true)
        repository.isPreloaded().test {
            val item = awaitItem()
            Truth.assertThat(item).isNotNull()
            Truth.assertThat(item).isTrue()
        }
    }

    @Test
    fun `when setPreloaded with account id should return the success`() = runTest {
        val response = repository.setPreloaded(true)
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        Truth.assertThat((response as Resource.Success).data).isTrue()
    }

    @Test
    fun `when getDefaultAccount for first time should return null`() = runTest {
        repository.getDefaultAccount().test {
            val item = awaitItem()
            Truth.assertThat(item).isNull()
        }
    }

    @Test
    fun `when setDefaultAccount with account id should return the success`() = runTest {
        val response = repository.setDefaultAccount("sampleId")
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        Truth.assertThat((response as Resource.Success).data).isTrue()
    }

    @Test
    fun `when getDefaultAccount after saving the item should return the saved value`() = runTest {

        val expectedItem = "sampleId"

        repository.setDefaultAccount(expectedItem)

        repository.getDefaultAccount().test {
            val item = awaitItem()
            Truth.assertThat(item).isNotNull()
            Truth.assertThat(item).isNotEmpty()
            Truth.assertThat(item).isEqualTo(expectedItem)
        }
    }

    @Test
    fun `when getDefaultExpenseCategory for first time should return null`() = runTest {
        repository.getDefaultExpenseCategory().test {
            val item = awaitItem()
            Truth.assertThat(item).isNull()
        }
    }

    @Test
    fun `when setDefaultExpenseCategory with category id should return the success`() = runTest {
        val response = repository.setDefaultExpenseCategory("sampleId")
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        Truth.assertThat((response as Resource.Success).data).isTrue()
    }

    @Test
    fun `when getDefaultExpenseCategory after saving the item should return the saved value`() =
        runTest {

            val expectedItem = "sampleId"

            repository.setDefaultExpenseCategory(expectedItem)

            repository.getDefaultExpenseCategory().test {
                val item = awaitItem()
                Truth.assertThat(item).isNotNull()
                Truth.assertThat(item).isNotEmpty()
                Truth.assertThat(item).isEqualTo(expectedItem)
            }
        }

    @Test
    fun `when getDefaultIncomeCategory for first time should return null`() = runTest {
        repository.getDefaultIncomeCategory().test {
            val item = awaitItem()
            Truth.assertThat(item).isNull()
        }
    }

    @Test
    fun `when setDefaultIncomeCategory with category id should return the success`() = runTest {
        val response = repository.setDefaultIncomeCategory("sampleId")
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        Truth.assertThat((response as Resource.Success).data).isTrue()
    }

    @Test
    fun `when getDefaultIncomeCategory after saving the item should return the saved value`() =
        runTest {

            val expectedItem = "sampleId"

            repository.setDefaultIncomeCategory(expectedItem)

            repository.getDefaultIncomeCategory().test {
                val item = awaitItem()
                Truth.assertThat(item).isNotNull()
                Truth.assertThat(item).isNotEmpty()
                Truth.assertThat(item).isEqualTo(expectedItem)
            }
        }
}