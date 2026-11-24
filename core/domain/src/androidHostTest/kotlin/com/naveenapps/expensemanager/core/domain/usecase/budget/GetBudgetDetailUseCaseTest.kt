package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.BudgetStatus
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.BudgetRepository
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_BUDGET
import com.naveenapps.expensemanager.core.testing.FAKE_CATEGORY
import com.naveenapps.expensemanager.core.testing.FAKE_EXPENSE_TRANSACTION
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetBudgetDetailUseCaseTest : BaseCoroutineTest() {

    private val budgetRepository: BudgetRepository = mock()
    private val currencyRepository: CurrencyRepository = mock()
    private val getBudgetTransactionsUseCase: GetBudgetTransactionsUseCase = mock()
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase = mock()

    private val currency = Currency(symbol = "₹", name = "Indian Rupee")
    private val food = FAKE_CATEGORY.copy(id = "food", name = "Food")
    private val travel = FAKE_CATEGORY.copy(id = "travel", name = "Travel")

    private val useCase = GetBudgetDetailUseCase(
        budgetRepository = budgetRepository,
        getCurrencyUseCase = GetCurrencyUseCase(currencyRepository),
        getFormattedAmountUseCase = GetFormattedAmountUseCase(currencyRepository),
        getBudgetTransactionsUseCase = getBudgetTransactionsUseCase,
        getTransactionWithFilterUseCase = getTransactionWithFilterUseCase,
    )

    override fun onCreate() {
        super.onCreate()
        whenever(currencyRepository.getSelectedCurrency()).thenReturn(flowOf(currency))
        whenever(currencyRepository.getFormattedCurrency(any())).thenAnswer { it.getArgument<Amount>(0) }
        whenever(getTransactionWithFilterUseCase.invoke()).thenReturn(flowOf(emptyList()))
    }

    @Test
    fun spendingOfABudgetIsTotalledAndSplitByCategory() = runTest {
        val budget = FAKE_BUDGET.copy(amount = 1000.0)
        whenever(budgetRepository.findBudgetByIdFlow(budget.id)).thenReturn(flowOf(budget))
        whenever(getBudgetTransactionsUseCase.invoke(budget)).thenReturn(
            Resource.Success(
                listOf(
                    FAKE_EXPENSE_TRANSACTION.copy(id = "1", amount = Amount(300.0), category = food),
                    FAKE_EXPENSE_TRANSACTION.copy(id = "2", amount = Amount(100.0), category = travel),
                    // Income in the budget's accounts doesn't count as spending.
                    FAKE_EXPENSE_TRANSACTION.copy(id = "3", amount = Amount(900.0), type = TransactionType.INCOME),
                ),
            ),
        )

        val detail = useCase.invoke(budget.id).first()!!

        assertThat(detail.transactionAmount.amount).isEqualTo(400.0)
        assertThat(detail.percent).isWithin(0.001f).of(40f)
        assertThat(detail.status).isEqualTo(BudgetStatus.WATCH)
        assertThat(detail.transactions?.map { it.id }).containsExactly("1", "2")
        val categories = detail.categorySpending!!.categoryTransactions
        assertThat(categories.map { it.category.id }).containsExactly("food", "travel").inOrder()
        assertThat(detail.categorySpending!!.totalAmount.amount).isEqualTo(400.0)
    }

    @Test
    fun aBudgetThatNoLongerExistsGivesNothing() = runTest {
        whenever(budgetRepository.findBudgetByIdFlow("gone")).thenReturn(flowOf(null))

        assertThat(useCase.invoke("gone").first()).isNull()
    }

    @Test
    fun failingToLoadTransactionsShowsNothingSpent() = runTest {
        val budget = FAKE_BUDGET.copy(amount = 1000.0)
        whenever(budgetRepository.findBudgetByIdFlow(budget.id)).thenReturn(flowOf(budget))
        whenever(getBudgetTransactionsUseCase.invoke(budget)).thenReturn(Resource.Error(Exception("db")))

        val detail = useCase.invoke(budget.id).first()!!

        assertThat(detail.transactionAmount.amount).isEqualTo(0.0)
        assertThat(detail.percent).isEqualTo(0f)
        assertThat(detail.transactions).isNull()
    }
}
