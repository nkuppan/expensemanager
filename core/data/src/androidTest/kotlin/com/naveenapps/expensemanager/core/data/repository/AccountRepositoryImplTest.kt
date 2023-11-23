package com.naveenapps.expensemanager.core.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.database.dao.AccountDao
import com.naveenapps.expensemanager.core.domain.repository.AccountRepository
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class AccountRepositoryImplTest : BaseCoroutineTest() {

    private lateinit var accountDao: AccountDao

    private lateinit var database: ExpenseManagerDatabase

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var accountRepository: AccountRepository

    override fun onCreate() {
        super.onCreate()

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ExpenseManagerDatabase::class.java
        ).allowMainThreadQueries().build()

        accountDao = database.accountDao()

        accountRepository = AccountRepositoryImpl(
            accountDao, AppCoroutineDispatchers(
                testCoroutineDispatcher.dispatcher,
                testCoroutineDispatcher.dispatcher,
                testCoroutineDispatcher.dispatcher
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        database.close()
    }

    @Test
    fun checkDatabaseObject() {
        Truth.assertThat(database).isNotNull()
        Truth.assertThat(accountDao).isNotNull()
    }

    @Test
    fun checkInsertSuccessCase() = runTest {
        addAccountAndAssert(FAKE_ACCOUNT)
    }

    @Test
    fun checkDeleteSuccessCase() = runTest {
        addAccountAndAssert(FAKE_ACCOUNT)
        deleteAccountAndAssert(FAKE_ACCOUNT)
    }

    @Test
    fun checkFindByIdSuccessCase() = runTest {
        addAccountAndAssert(FAKE_ACCOUNT)
        findAccountAndAssert(FAKE_ACCOUNT.id)
    }

    @Test
    fun checkGetAllAccountSuccessCase() = runTest {
        addAccountAndAssert(FAKE_ACCOUNT)
        findAccountAndAssert(FAKE_ACCOUNT.id)
    }

    @Test
    fun checkGetAllAccountFlowAfterInsertCase() = runTest {
        accountRepository.getAccounts().test {
            val data = awaitItem()
            Truth.assertThat(data).isEmpty()

            addAccountAndAssert(FAKE_ACCOUNT)

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isEmpty()
            val firstItem = secondItem.first()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem.id).isEqualTo(FAKE_ACCOUNT.id)

            deleteAccountAndAssert(FAKE_ACCOUNT)
            val newData = awaitItem()
            Truth.assertThat(newData).isEmpty()
        }
    }

    @Test
    fun checkFindAccountErrorCase() = runTest {
        val newResult = accountRepository.findAccount("Unknown id")
        Truth.assertThat(newResult).isNotNull()
        Truth.assertThat(newResult).isInstanceOf(Resource.Error::class.java)
        val foundData = (newResult as Resource.Error).exception
        Truth.assertThat(foundData).isNotNull()
    }

    @Test
    fun checkDeleteAccountErrorCase() = runTest {
        val newResult = accountRepository.deleteAccount(FAKE_ACCOUNT)
        Truth.assertThat(newResult).isNotNull()
        Truth.assertThat(newResult).isInstanceOf(Resource.Success::class.java)
        val foundData = (newResult as Resource.Success).data
        Truth.assertThat(foundData).isFalse()
    }

    @Test
    fun checkUpdateCase() = runTest {
        addAccountAndAssert(FAKE_ACCOUNT)
        val name = "New"
        val fakeInsert = FAKE_ACCOUNT.copy(name = name)

        val newResult = accountRepository.updateAccount(fakeInsert)
        Truth.assertThat(newResult).isNotNull()
        Truth.assertThat(newResult).isInstanceOf(Resource.Success::class.java)
        val data = (newResult as Resource.Success).data
        Truth.assertThat(data).isNotNull()
        Truth.assertThat(data).isTrue()

        findAccountAndAssert(FAKE_ACCOUNT.id)
    }

    private suspend fun findAccountAndAssert(accountId: String) {
        val newResult = accountRepository.findAccount(accountId)
        Truth.assertThat(newResult).isNotNull()
        Truth.assertThat(newResult).isInstanceOf(Resource.Success::class.java)
        val foundData = (newResult as Resource.Success).data
        Truth.assertThat(foundData).isNotNull()
        Truth.assertThat(foundData.id).isEqualTo(accountId)
    }

    private suspend fun addAccountAndAssert(account: Account) {
        val result = accountRepository.addAccount(account)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Success::class.java)
        val data = (result as Resource.Success).data
        Truth.assertThat(data).isNotNull()
        Truth.assertThat(data).isTrue()
    }

    private suspend fun deleteAccountAndAssert(account: Account) {
        val result = accountRepository.deleteAccount(account)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Success::class.java)
        val data = (result as Resource.Success).data
        Truth.assertThat(data).isNotNull()
        Truth.assertThat(data).isTrue()
    }
}