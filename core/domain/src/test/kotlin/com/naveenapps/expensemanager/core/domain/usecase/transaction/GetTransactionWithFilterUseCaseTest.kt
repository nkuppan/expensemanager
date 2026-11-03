package com.naveenapps.expensemanager.core.domain.usecase.transaction

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import com.naveenapps.expensemanager.core.testing.FAKE_EXPENSE_TRANSACTION
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class GetTransactionWithFilterUseCaseTest {

    private val accountRepository: AccountRepository = mock()
    private val categoryRepository: CategoryRepository = mock()
    private val settingsRepository: SettingsRepository = mock()
    private val getDateRangeUseCase: GetDateRangeUseCase = mock()
    private val transactionRepository: TransactionRepository = mock()

    private val transactions = MutableStateFlow(listOf(FAKE_EXPENSE_TRANSACTION))

    private fun TestScope.useCase(): GetTransactionWithFilterUseCase {
        whenever(settingsRepository.getTransactionTypes()).thenReturn(flowOf(null))
        whenever(settingsRepository.getAccounts()).thenReturn(flowOf(listOf("1")))
        whenever(settingsRepository.getCategories()).thenReturn(flowOf(listOf("1")))
        whenever(getDateRangeUseCase.invoke()).thenReturn(
            flowOf(DateRangeModel("This month", "", DateRangeType.THIS_MONTH, listOf(0L, 100L))),
        )
        whenever(
            transactionRepository.getFilteredTransaction(any(), any(), any(), any(), any()),
        ).thenReturn(transactions)
        return GetTransactionWithFilterUseCase(
            accountRepository = accountRepository,
            categoryRepository = categoryRepository,
            settingsRepository = settingsRepository,
            getDateRangeUseCase = getDateRangeUseCase,
            transactionRepository = transactionRepository,
            sharingScope = backgroundScope,
        )
    }

    @Test
    fun `every collector of the selected period shares one query`() = runTest(UnconfinedTestDispatcher()) {
        val useCase = useCase()
        val received = mutableListOf<Int>()

        // The dashboard summary, both category donuts and budgets all collect at once.
        repeat(4) {
            backgroundScope.launch { useCase.invoke().collect { received += it.orEmpty().size } }
        }
        runCurrent()

        assertThat(received).containsExactly(1, 1, 1, 1)
        verify(transactionRepository, times(1)).getFilteredTransaction(any(), any(), any(), any(), any())

        // A save re-emits once, and every collector gets it.
        transactions.value = listOf(FAKE_EXPENSE_TRANSACTION, FAKE_EXPENSE_TRANSACTION.copy(id = "2"))
        runCurrent()
        assertThat(received).containsExactly(1, 1, 1, 1, 2, 2, 2, 2)
    }

    @Test
    fun `the query stops after the last collector leaves and starts fresh on return`() =
        runTest(UnconfinedTestDispatcher()) {
            val useCase = useCase()

            useCase.invoke().test { assertThat(awaitItem()).hasSize(1) }
            advanceTimeBy(6_000)
            runCurrent()

            transactions.value = emptyList()
            // No stale list is replayed first: the next collector sees only the current data.
            useCase.invoke().test { assertThat(awaitItem()).isEmpty() }

            verify(transactionRepository, times(2)).getFilteredTransaction(any(), any(), any(), any(), any())
        }
}
