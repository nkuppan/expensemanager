package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_ACCOUNT
import com.naveenapps.expensemanager.core.testing.FAKE_BUDGET
import com.naveenapps.expensemanager.core.testing.FAKE_CATEGORY
import com.naveenapps.expensemanager.core.testing.FAKE_EXPENSE_TRANSACTION
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetBudgetTransactionsUseCaseTest : BaseCoroutineTest() {

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
        whenever(categoryRepository.getCategories()).thenReturn(flowOf(listOf(FAKE_CATEGORY)))
        whenever(accountRepository.getAccounts()).thenReturn(flowOf(listOf(FAKE_ACCOUNT)))
        whenever(
            transactionRepository.getFilteredTransaction(
                any(),
                any(),
                any(),
                any(),
                any(),
            ),
        ).thenReturn(flowOf(listOf(FAKE_EXPENSE_TRANSACTION)))

        val response = getBudgetTransactionsUseCase.invoke(FAKE_BUDGET)
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        Truth.assertThat((response as Resource.Success).data).isNotEmpty()
        Truth.assertThat(response.data).hasSize(1)
    }

    @Test
    fun whenThereIsNoDataItShouldBeEmpty() = runTest {
        whenever(categoryRepository.getCategories()).thenReturn(flowOf(listOf(FAKE_CATEGORY)))
        whenever(accountRepository.getAccounts()).thenReturn(flowOf(listOf(FAKE_ACCOUNT)))
        whenever(
            transactionRepository.getFilteredTransaction(
                any(),
                any(),
                any(),
                any(),
                any(),
            ),
        ).thenReturn(flowOf(listOf()))

        val response = getBudgetTransactionsUseCase.invoke(FAKE_BUDGET)
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        Truth.assertThat((response as Resource.Success).data).isEmpty()
    }
}
