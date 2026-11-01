package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYearKey
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_ACCOUNT
import com.naveenapps.expensemanager.core.testing.FAKE_BUDGET
import com.naveenapps.expensemanager.core.testing.FAKE_CATEGORY
import com.naveenapps.expensemanager.core.testing.FAKE_EXPENSE_TRANSACTION
import java.util.Calendar
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
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

    // The repository is mocked, so these check the date range the use case asks for. A month must
    // run from its first millisecond to its last one (the query uses inclusive BETWEEN). The old
    // range started on the previous month's last day and stopped before the month's last 1–4 days.

    @Test
    fun monthlyBudgetCoversTheWholeMonthIncludingTheLastDay() = runTest {
        stubEmptyRepositories()
        val budget = FAKE_BUDGET.copy(selectedMonth = localDate(2026, Calendar.OCTOBER, 15).time.toMonthAndYearKey())

        getBudgetTransactionsUseCase.invoke(budget)

        val (start, end) = capturedRange()
        Truth.assertThat(start).isEqualTo(localDate(2026, Calendar.OCTOBER, 1).timeInMillis)
        Truth.assertThat(end).isEqualTo(localDate(2026, Calendar.NOVEMBER, 1).timeInMillis - 1)
        Truth.assertThat(localDate(2026, Calendar.OCTOBER, 31, hour = 23, minute = 59).timeInMillis).isAtMost(end)
    }

    @Test
    fun monthlyBudgetForMarchKeepsItsLastFourDays() = runTest {
        stubEmptyRepositories()
        val budget = FAKE_BUDGET.copy(selectedMonth = localDate(2026, Calendar.MARCH, 1).time.toMonthAndYearKey())

        getBudgetTransactionsUseCase.invoke(budget)

        val (start, end) = capturedRange()
        Truth.assertThat(start).isEqualTo(localDate(2026, Calendar.MARCH, 1).timeInMillis)
        Truth.assertThat(end).isEqualTo(localDate(2026, Calendar.APRIL, 1).timeInMillis - 1)
    }

    @Test
    fun monthlyBudgetForLeapFebruaryIncludesThe29th() = runTest {
        stubEmptyRepositories()
        val budget = FAKE_BUDGET.copy(selectedMonth = localDate(2028, Calendar.FEBRUARY, 10).time.toMonthAndYearKey())

        getBudgetTransactionsUseCase.invoke(budget)

        val (start, end) = capturedRange()
        Truth.assertThat(start).isEqualTo(localDate(2028, Calendar.FEBRUARY, 1).timeInMillis)
        Truth.assertThat(end).isEqualTo(localDate(2028, Calendar.MARCH, 1).timeInMillis - 1)
    }

    @Test
    fun yearlyBudgetCoversTheWholeYearIncludingThe31stOfDecember() = runTest {
        stubEmptyRepositories()
        val budget = FAKE_BUDGET.copy(selectedMonth = "2026", periodType = BudgetPeriod.YEARLY)

        getBudgetTransactionsUseCase.invoke(budget)

        val (start, end) = capturedRange()
        Truth.assertThat(start).isEqualTo(localDate(2026, Calendar.JANUARY, 1).timeInMillis)
        Truth.assertThat(end).isEqualTo(localDate(2027, Calendar.JANUARY, 1).timeInMillis - 1)
    }

    private fun stubEmptyRepositories() {
        whenever(categoryRepository.getCategories()).thenReturn(flowOf(listOf(FAKE_CATEGORY)))
        whenever(accountRepository.getAccounts()).thenReturn(flowOf(listOf(FAKE_ACCOUNT)))
        whenever(
            transactionRepository.getFilteredTransaction(any(), any(), any(), any(), any()),
        ).thenReturn(flowOf(listOf()))
    }

    private fun capturedRange(): Pair<Long, Long> {
        val start = argumentCaptor<Long>()
        val end = argumentCaptor<Long>()
        verify(transactionRepository).getFilteredTransaction(any(), any(), any(), start.capture(), end.capture())
        return start.firstValue to end.firstValue
    }

    /** A wall-clock time in the JVM's default time zone, the same zone the app's date helpers use. */
    private fun localDate(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Calendar = Calendar.getInstance().apply {
        clear()
        set(year, month, day, hour, minute, 0)
    }
}
