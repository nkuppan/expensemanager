package com.naveenapps.expensemanager.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.database.dao.AccountDao
import com.naveenapps.expensemanager.core.database.dao.BudgetDao
import com.naveenapps.expensemanager.core.database.dao.CategoryDao
import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.BudgetRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_BUDGET
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class BudgetRepositoryImplTest : BaseCoroutineTest() {

    private lateinit var categoryDao: CategoryDao
    private lateinit var accountDao: AccountDao
    private lateinit var budgetDao: BudgetDao

    private lateinit var database: ExpenseManagerDatabase

    private lateinit var budgetRepository: BudgetRepository

    override fun onCreate() {
        super.onCreate()

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ExpenseManagerDatabase::class.java,
        ).allowMainThreadQueries().build()

        categoryDao = database.categoryDao()
        accountDao = database.accountDao()
        budgetDao = database.budgetDao()

        budgetRepository = BudgetRepositoryImpl(
            budgetDao,
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

    @Test
    fun checkDatabaseObject() {
        Truth.assertThat(database).isNotNull()
        Truth.assertThat(categoryDao).isNotNull()
        Truth.assertThat(accountDao).isNotNull()
        Truth.assertThat(budgetDao).isNotNull()
    }

    @Test
    fun checkInsertSuccessCase() = runTest {
        addBudgetAndAssert(FAKE_BUDGET)
    }

    @Test
    fun checkDeleteSuccessCase() = runTest {
        addBudgetAndAssert(FAKE_BUDGET)
        deleteBudgetAndAssert(FAKE_BUDGET)
    }

    @Test
    fun checkFindByIdSuccessCase() = runTest {
        addBudgetAndAssert(FAKE_BUDGET)
        findBudgetAndAssert(FAKE_BUDGET.id)
    }

    @Test
    fun checkGetAllBudgetSuccessCase() = runTest {
        addBudgetAndAssert(FAKE_BUDGET)
        findBudgetAndAssert(FAKE_BUDGET.id)
    }

    @Test
    fun checkGetAllBudgetFlowAfterInsertCase() = runTest {
        budgetRepository.getBudgets().test {
            val data = awaitItem()
            Truth.assertThat(data).isEmpty()

            addBudgetAndAssert(FAKE_BUDGET)

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isNotEmpty()
            val firstItem = secondItem.first()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem.id).isEqualTo(FAKE_BUDGET.id)

            deleteBudgetAndAssert(FAKE_BUDGET)
            val newData = awaitItem()
            Truth.assertThat(newData).isEmpty()
        }
    }

    @Test
    fun checkFindBudgetErrorCase() = runTest {
        val newResult = budgetRepository.findBudgetById("Unknown id")
        Truth.assertThat(newResult).isNotNull()
        Truth.assertThat(newResult).isInstanceOf(Resource.Error::class.java)
        val foundData = (newResult as Resource.Error).exception
        Truth.assertThat(foundData).isNotNull()
    }

    @Test
    fun checkDeleteBudgetErrorCase() = runTest {
        val newResult = budgetRepository.deleteBudget(FAKE_BUDGET)
        Truth.assertThat(newResult).isNotNull()
        Truth.assertThat(newResult).isInstanceOf(Resource.Success::class.java)
        val foundData = (newResult as Resource.Success).data
        Truth.assertThat(foundData).isFalse()
    }

    @Test
    fun checkUpdateCase() = runTest {
        addBudgetAndAssert(FAKE_BUDGET)
        val name = "New"
        val fakeInsert = FAKE_BUDGET.copy(name = name)

        val newResult = budgetRepository.updateBudget(fakeInsert)
        Truth.assertThat(newResult).isNotNull()
        Truth.assertThat(newResult).isInstanceOf(Resource.Success::class.java)
        val data = (newResult as Resource.Success).data
        Truth.assertThat(data).isNotNull()
        Truth.assertThat(data).isTrue()

        findBudgetAndAssert(FAKE_BUDGET.id)
    }

    @Test
    fun whenFindTheBudgetAsFlow() = runTest {
        budgetRepository.findBudgetByIdFlow(FAKE_BUDGET.id).test {
            val response = awaitItem()
            Truth.assertThat(response).isNull()

            addBudgetAndAssert(FAKE_BUDGET)

            val updatedItem = awaitItem()
            Truth.assertThat(updatedItem).isNotNull()
            Truth.assertThat(updatedItem?.id).isEqualTo(FAKE_BUDGET.id)

            deleteBudgetAndAssert(FAKE_BUDGET)

            val afterUpdateItem = awaitItem()
            Truth.assertThat(afterUpdateItem).isNull()
        }
    }

    private suspend fun findBudgetAndAssert(accountId: String) {
        val newResult = budgetRepository.findBudgetById(accountId)
        Truth.assertThat(newResult).isNotNull()
        Truth.assertThat(newResult).isInstanceOf(Resource.Success::class.java)
        val foundData = (newResult as Resource.Success).data
        Truth.assertThat(foundData).isNotNull()
        Truth.assertThat(foundData.id).isEqualTo(accountId)
    }

    private suspend fun addBudgetAndAssert(account: Budget) {
        val result = budgetRepository.addBudget(account)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Success::class.java)
        val data = (result as Resource.Success).data
        Truth.assertThat(data).isNotNull()
        Truth.assertThat(data).isTrue()
    }

    private suspend fun deleteBudgetAndAssert(account: Budget) {
        val result = budgetRepository.deleteBudget(account)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Success::class.java)
        val data = (result as Resource.Success).data
        Truth.assertThat(data).isNotNull()
        Truth.assertThat(data).isTrue()
    }
}
