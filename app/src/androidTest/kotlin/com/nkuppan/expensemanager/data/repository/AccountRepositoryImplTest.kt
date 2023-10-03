package com.nkuppan.expensemanager.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.nkuppan.expensemanager.common.testing.BaseCoroutineTest
import com.nkuppan.expensemanager.common.utils.AppCoroutineDispatchers
import com.nkuppan.expensemanager.data.db.ExpenseManagerDatabase
import com.nkuppan.expensemanager.data.db.dao.AccountDao
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.AccountRepository
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
            accountDao,
            AppCoroutineDispatchers(
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
        val result = accountRepository.addAccount(FAKE_ACCOUNT)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Success::class.java)
        val data = (result as Resource.Success).data
        Truth.assertThat(data).isNotNull()
        Truth.assertThat(data).isTrue()
    }

    @Test
    fun checkDeleteSuccessCase() = runTest {
        var result = accountRepository.addAccount(FAKE_ACCOUNT)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Success::class.java)
        var data = (result as Resource.Success).data
        Truth.assertThat(data).isNotNull()
        Truth.assertThat(data).isTrue()

        result = accountRepository.deleteAccount(FAKE_ACCOUNT)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Success::class.java)
        data = (result as Resource.Success).data
        Truth.assertThat(data).isNotNull()
        Truth.assertThat(data).isTrue()
    }

    @Test
    fun checkFindByIdSuccessCase() = runTest {
        val result = accountRepository.addAccount(FAKE_ACCOUNT)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Success::class.java)
        val data = (result as Resource.Success).data
        Truth.assertThat(data).isNotNull()
        Truth.assertThat(data).isTrue()

        val newResult = accountRepository.findAccount(FAKE_ACCOUNT.id)
        Truth.assertThat(newResult).isNotNull()
        Truth.assertThat(newResult).isInstanceOf(Resource.Success::class.java)
        val foundData = (newResult as Resource.Success).data
        Truth.assertThat(foundData).isNotNull()
        Truth.assertThat(foundData.id).isEqualTo(FAKE_ACCOUNT.id)
    }

    @Test
    fun checkGetAllAccountSuccessCase() = runTest {
        val result = accountRepository.addAccount(FAKE_ACCOUNT)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Success::class.java)
        val data = (result as Resource.Success).data
        Truth.assertThat(data).isNotNull()
        Truth.assertThat(data).isTrue()

        val newResult = accountRepository.findAccount(FAKE_ACCOUNT.id)
        Truth.assertThat(newResult).isNotNull()
        Truth.assertThat(newResult).isInstanceOf(Resource.Success::class.java)
        val foundData = (newResult as Resource.Success).data
        Truth.assertThat(foundData).isNotNull()
        Truth.assertThat(foundData.id).isEqualTo(FAKE_ACCOUNT.id)
    }
}