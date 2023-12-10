package com.naveenapps.expensemanager.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.data.mappers.toEntityModel
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.database.dao.AccountDao
import com.naveenapps.expensemanager.core.database.dao.CategoryDao
import com.naveenapps.expensemanager.core.database.dao.TransactionDao
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class TransactionRepositoryImplTest : BaseCoroutineTest() {

    private lateinit var accountDao: AccountDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var transactionDao: TransactionDao

    private lateinit var database: ExpenseManagerDatabase

    private lateinit var transactionRepository: TransactionRepository

    override fun onCreate() {
        super.onCreate()

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ExpenseManagerDatabase::class.java,
        ).allowMainThreadQueries().build()

        transactionDao = database.transactionDao()
        accountDao = database.accountDao()
        categoryDao = database.categoryDao()

        transactionRepository = TransactionRepositoryImpl(
            transactionDao,
            accountDao,
            categoryDao,
            AppCoroutineDispatchers(
                testCoroutineDispatcher.dispatcher,
                testCoroutineDispatcher.dispatcher,
                testCoroutineDispatcher.dispatcher,
            ),
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        database.close()
    }

    /**
     * This below test will throw an error. There is no account and category available in the
     * database.
     */
    @Test
    fun transactionInsertFailureCase() = runTest {
        val result = transactionRepository.addTransaction(FAKE_EXPENSE_TRANSACTION)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Error::class.java)
        val exception = (result as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
    }

    @Test
    fun transactionInsertSuccessCase() = runTest {
        setupBasicAppData()
        insertTransactionAndAssert(FAKE_EXPENSE_TRANSACTION)
    }

    @Test
    fun expenseTransactionInsertSuccessAndCheckTransactionValues() = runTest {
        setupBasicAppData()
        insertTransactionAndAssert(FAKE_EXPENSE_TRANSACTION)
        validateTransaction(FAKE_EXPENSE_TRANSACTION)
    }

    @Test
    fun incomeTransactionInsertSuccessAndCheckTransactionValues() = runTest {
        setupBasicAppData()
        insertTransactionAndAssert(FAKE_INCOME_TRANSACTION)
        validateTransaction(FAKE_INCOME_TRANSACTION)
    }

    @Test
    fun transferTransactionInsertSuccessAndCheckTransactionValues() = runTest {
        setupBasicAppData()
        insertTransactionAndAssert(FAKE_TRANSFER_TRANSACTION)
        validateTransaction(FAKE_TRANSFER_TRANSACTION)
    }

    @Test
    fun deleteTransactionSuccessAndCheck() = runTest {
        setupBasicAppData()
        insertTransactionAndAssert(FAKE_INCOME_TRANSACTION)
        deleteTransactionAndAssert(FAKE_INCOME_TRANSACTION)
    }

    private suspend fun validateTransaction(addedTransaction: Transaction) {
        val response = transactionRepository.findTransactionById(addedTransaction.id)
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        val transaction = (response as Resource.Success).data
        Truth.assertThat(transaction).isNotNull()
        Truth.assertThat(transaction.type).isEqualTo(addedTransaction.type)
        Truth.assertThat(transaction.amount).isEqualTo(addedTransaction.amount)
        Truth.assertThat(transaction.fromAccountId).isEqualTo(addedTransaction.fromAccountId)
        Truth.assertThat(transaction.fromAccount).isNotNull()
        Truth.assertThat(transaction.fromAccount.amount).isEqualTo(
            when (addedTransaction.type) {
                TransactionType.INCOME -> (addedTransaction.amount.amount)
                TransactionType.EXPENSE -> (addedTransaction.amount.amount * -1)
                else -> (addedTransaction.amount.amount)
            },
        )
        if (addedTransaction.type == TransactionType.TRANSFER) {
            Truth.assertThat(transaction.toAccountId).isEqualTo(addedTransaction.toAccountId)
            Truth.assertThat(transaction.toAccount).isNotNull()
        }
    }

    private suspend fun insertTransactionAndAssert(transaction: Transaction) {
        val result = transactionRepository.addTransaction(transaction)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Success::class.java)
        val data = (result as Resource.Success).data
        Truth.assertThat(data).isNotNull()
        Truth.assertThat(data).isTrue()
    }

    private suspend fun deleteTransactionAndAssert(transaction: Transaction) {
        val result = transactionRepository.deleteTransaction(transaction)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Success::class.java)
        val data = (result as Resource.Success).data
        Truth.assertThat(data).isNotNull()
        Truth.assertThat(data).isTrue()
    }

    private suspend fun setupBasicAppData() {
        accountDao.insert(FAKE_ACCOUNT.toEntityModel())
        accountDao.insert(FAKE_SECOND_ACCOUNT.toEntityModel())
        categoryDao.insert(FAKE_CATEGORY.toEntityModel())
        categoryDao.insert(FAKE_SECOND_CATEGORY.toEntityModel())
    }
}
