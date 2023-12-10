package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.common.utils.toMonthYear
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

class GetBudgetTransactionsUseCaseTest : BaseCoroutineTest() {

    private val category = Category(
        id = "1",
        name = "Test",
        type = CategoryType.EXPENSE,
        storedIcon = StoredIcon(
            name = "#FFFFFF",
            backgroundColor = "#FFFFFF",
        ),
        createdOn = Date(),
        updatedOn = Date(),
    )

    private val account = Account(
        id = "1",
        name = "Sample",
        type = AccountType.CREDIT,
        storedIcon = StoredIcon("ic_account", "#ffffff"),
        createdOn = Date(),
        updatedOn = Date(),
    )

    private val budget = Budget(
        id = "1",
        name = "Sample",
        amount = 5000.0,
        selectedMonth = Date().toMonthYear(),
        accounts = emptyList(),
        categories = emptyList(),
        isAllAccountsSelected = true,
        isAllCategoriesSelected = true,
        storedIcon = StoredIcon("ic_account", "#ffffff"),
        createdOn = Date(),
        updatedOn = Date(),
    )

    private val transaction = Transaction(
        id = "1",
        notes = "Test",
        categoryId = "1",
        fromAccountId = "1",
        toAccountId = "2",
        type = TransactionType.INCOME,
        amount = Amount(200.0),
        imagePath = "",
        createdOn = Date(),
        updatedOn = Date(),
    )

    private val categoryRepository: CategoryRepository = mock()
    private val accountRepository: AccountRepository = mock()
    private val transactionRepository: TransactionRepository = mock()

    private lateinit var getBudgetTransactionsUseCase: GetBudgetTransactionsUseCase

    override fun onCreate() {
        super.onCreate()

        getBudgetTransactionsUseCase = GetBudgetTransactionsUseCase(
            categoryRepository,
            accountRepository,
            transactionRepository,
        )
    }

    @Test
    fun whenAllDataAvailableShouldReturnListOfTransactionAssociatedWithBudget() = runTest {
        whenever(categoryRepository.getCategories()).thenReturn(flowOf(listOf(category)))
        whenever(accountRepository.getAccounts()).thenReturn(flowOf(listOf(account)))
        whenever(
            transactionRepository.getFilteredTransaction(
                any(),
                any(),
                any(),
                any(),
                any(),
            ),
        ).thenReturn(flowOf(listOf(transaction)))

        val response = getBudgetTransactionsUseCase.invoke(budget)
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        Truth.assertThat((response as Resource.Success).data).isNotEmpty()
        Truth.assertThat(response.data).hasSize(1)
    }

    @Test
    fun whenThereIsNoDataItShouldBeEmpty() = runTest {
        whenever(categoryRepository.getCategories()).thenReturn(flowOf(listOf(category)))
        whenever(accountRepository.getAccounts()).thenReturn(flowOf(listOf(account)))
        whenever(
            transactionRepository.getFilteredTransaction(
                any(),
                any(),
                any(),
                any(),
                any(),
            ),
        ).thenReturn(flowOf(listOf()))

        val response = getBudgetTransactionsUseCase.invoke(budget)
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        Truth.assertThat((response as Resource.Success).data).isEmpty()
    }
}
