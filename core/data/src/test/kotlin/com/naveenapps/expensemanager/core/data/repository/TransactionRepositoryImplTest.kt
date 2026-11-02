package com.naveenapps.expensemanager.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.data.mappers.toEntityModel
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.database.dao.AccountDao
import com.naveenapps.expensemanager.core.database.dao.CategoryDao
import com.naveenapps.expensemanager.core.database.dao.TransactionDao
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_ACCOUNT
import com.naveenapps.expensemanager.core.testing.FAKE_CATEGORY
import com.naveenapps.expensemanager.core.testing.FAKE_EXPENSE_TRANSACTION
import com.naveenapps.expensemanager.core.testing.FAKE_INCOME_TRANSACTION
import com.naveenapps.expensemanager.core.testing.FAKE_SECOND_ACCOUNT
import com.naveenapps.expensemanager.core.testing.FAKE_SECOND_CATEGORY
import com.naveenapps.expensemanager.core.testing.FAKE_TRANSFER_TRANSACTION
import java.util.Date
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

    @Test
    fun getAllTransactions() = runTest {
        setupBasicAppData()

        insertTransactionAndAssert(FAKE_INCOME_TRANSACTION)

        transactionRepository.getAllTransaction().test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem).isNotEmpty()
            Truth.assertThat(firstItem).hasSize(1)
            val transaction = firstItem?.firstOrNull()
            Truth.assertThat(transaction).isNotNull()
            Truth.assertThat(transaction!!.id).isEqualTo(FAKE_INCOME_TRANSACTION.id)
        }
    }

    @Test
    fun updateTransaction() = runTest {
        setupBasicAppData()

        insertTransactionAndAssert(FAKE_INCOME_TRANSACTION)
        val updatedTransaction = FAKE_INCOME_TRANSACTION.copy(notes = "Sample Notes")
        updateTransactionAndAssert(updatedTransaction)

        validateTransaction(updatedTransaction)
    }

    @Test
    fun updateTransactionAmountKeepsAccountBalanceConsistent() = runTest {
        setupBasicAppData()

        insertTransactionAndAssert(FAKE_EXPENSE_TRANSACTION)
        updateTransactionAndAssert(FAKE_EXPENSE_TRANSACTION.copy(amount = Amount(50.0)))

        // The old 200 is reversed and only the new 50 is applied.
        Truth.assertThat(accountDao.findById(FAKE_ACCOUNT.id)!!.amount).isEqualTo(-50.0)
    }

    @Test
    fun updateTransactionToAnotherAccountMovesBalance() = runTest {
        setupBasicAppData()

        insertTransactionAndAssert(FAKE_EXPENSE_TRANSACTION)
        updateTransactionAndAssert(
            FAKE_EXPENSE_TRANSACTION.copy(fromAccountId = FAKE_SECOND_ACCOUNT.id),
        )

        Truth.assertThat(accountDao.findById(FAKE_ACCOUNT.id)!!.amount).isEqualTo(0.0)
        Truth.assertThat(accountDao.findById(FAKE_SECOND_ACCOUNT.id)!!.amount).isEqualTo(-200.0)
    }

    @Test
    fun deleteTransactionRestoresAccountBalances() = runTest {
        setupBasicAppData()

        insertTransactionAndAssert(FAKE_TRANSFER_TRANSACTION)
        deleteTransactionAndAssert(FAKE_TRANSFER_TRANSACTION)

        Truth.assertThat(accountDao.findById(FAKE_ACCOUNT.id)!!.amount).isEqualTo(0.0)
        Truth.assertThat(accountDao.findById(FAKE_SECOND_ACCOUNT.id)!!.amount).isEqualTo(0.0)
    }

    @Test
    fun deleteUnknownTransactionReportsFailure() = runTest {
        setupBasicAppData()

        val result = transactionRepository.deleteTransaction(FAKE_EXPENSE_TRANSACTION)

        Truth.assertThat(result).isInstanceOf(Resource.Success::class.java)
        Truth.assertThat((result as Resource.Success).data).isFalse()
        Truth.assertThat(accountDao.findById(FAKE_ACCOUNT.id)!!.amount).isEqualTo(0.0)
    }

    @Test
    fun findTransactionForUnknownId() = runTest {
        setupBasicAppData()
        val result = transactionRepository.findTransactionById("unknownId")
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Error::class.java)
        Truth.assertThat((result as Resource.Error).exception).isNotNull()
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
        Truth.assertThat(transaction.notes).isEqualTo(addedTransaction.notes)
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

    private suspend fun updateTransactionAndAssert(transaction: Transaction) {
        val result = transactionRepository.updateTransaction(transaction)
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

    @Test
    fun frequentTransactionsGroupIdenticalEntriesMostUsedFirst() = runTest {
        setupBasicAppData()
        val tea = FAKE_EXPENSE_TRANSACTION.copy(notes = "Tea", amount = Amount(20.0))
        val lunch = FAKE_EXPENSE_TRANSACTION.copy(notes = "Lunch", amount = Amount(150.0))
        listOf(
            tea.copy(id = "t1"),
            tea.copy(id = "t2", notes = "Tea "),
            tea.copy(id = "t3"),
            lunch.copy(id = "l1"),
            lunch.copy(id = "l2"),
            // Logged once only, so not frequent.
            FAKE_EXPENSE_TRANSACTION.copy(id = "o1", notes = "Movie", amount = Amount(300.0)),
            // Same details but older than the window.
            tea.copy(id = "old", createdOn = Date(0), updatedOn = Date(0)),
        ).forEach { transactionRepository.addTransaction(it) }

        transactionRepository.getFrequentTransactions(
            transactionTypes = listOf(TransactionType.EXPENSE, TransactionType.INCOME),
            since = System.currentTimeMillis() - 86_400_000L,
            minCount = 2,
            limit = 6,
        ).test {
            val frequent = awaitItem()
            Truth.assertThat(frequent.map { it.notes to it.useCount })
                .containsExactly("Tea" to 3, "Lunch" to 2)
                .inOrder()
            Truth.assertThat(frequent.first().amount).isEqualTo(20.0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun setupBasicAppData() {
        accountDao.insert(FAKE_ACCOUNT.toEntityModel())
        accountDao.insert(FAKE_SECOND_ACCOUNT.toEntityModel())
        categoryDao.insert(FAKE_CATEGORY.toEntityModel())
        categoryDao.insert(FAKE_SECOND_CATEGORY.toEntityModel())
    }
}
