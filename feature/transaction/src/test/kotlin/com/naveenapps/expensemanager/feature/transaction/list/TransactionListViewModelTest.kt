package com.naveenapps.expensemanager.feature.transaction.list

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.domain.usecase.recurring.GetUpcomingRecurringTransactionsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.recurring.ProcessRecurringTransactionsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.AddTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.DeleteTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.ImageStorageRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_ACCOUNT
import com.naveenapps.expensemanager.core.testing.FAKE_CATEGORY
import com.naveenapps.expensemanager.core.testing.FAKE_EXPENSE_TRANSACTION
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class TransactionListViewModelTest : BaseCoroutineTest() {

    private val getCurrencyUseCase: GetCurrencyUseCase = mock()
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase = mock()
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase = mock()
    private val deleteTransactionUseCase: DeleteTransactionUseCase = mock()
    private val addTransactionUseCase: AddTransactionUseCase = mock()
    private val imageStorageRepository: ImageStorageRepository = mock()
    private val navigator: AppComposeNavigator = mock()
    private val getUpcomingRecurringTransactionsUseCase: GetUpcomingRecurringTransactionsUseCase = mock()

    private val groceries = FAKE_EXPENSE_TRANSACTION.copy(
        id = "groceries",
        notes = "Weekly groceries",
        attachments = listOf("receipt.jpg"),
    ).apply {
        category = FAKE_CATEGORY
        fromAccount = FAKE_ACCOUNT
    }
    private val rent = FAKE_EXPENSE_TRANSACTION.copy(id = "rent", notes = "Rent").apply {
        category = FAKE_CATEGORY
        fromAccount = FAKE_ACCOUNT
    }

    private fun viewModel(
        transactions: List<Transaction> = listOf(groceries, rent),
        upcoming: List<Transaction> = emptyList(),
    ): TransactionListViewModel {
        whenever(getUpcomingRecurringTransactionsUseCase.invoke(any(), any())).thenReturn(flowOf(upcoming))
        whenever(getCurrencyUseCase.invoke()).thenReturn(flowOf(Currency("$", "USD")))
        whenever(getFormattedAmountUseCase.invoke(any(), any())).thenReturn(Amount(0.0, "$0"))
        whenever(getTransactionWithFilterUseCase.invoke()).thenReturn(flowOf(transactions))
        return TransactionListViewModel(
            getCurrencyUseCase = getCurrencyUseCase,
            getFormattedAmountUseCase = getFormattedAmountUseCase,
            getTransactionWithFilterUseCase = getTransactionWithFilterUseCase,
            getUpcomingRecurringTransactionsUseCase = getUpcomingRecurringTransactionsUseCase,
            appCoroutineDispatchers = AppCoroutineDispatchers(
                testCoroutineDispatcher.dispatcher,
                testCoroutineDispatcher.dispatcher,
                testCoroutineDispatcher.dispatcher,
            ),
            appComposeNavigator = navigator,
            deleteTransactionUseCase = deleteTransactionUseCase,
            addTransactionUseCase = addTransactionUseCase,
            imageStorageRepository = imageStorageRepository,
        )
    }

    private fun TransactionListState.ids() = transactionListItem
        .filterIsInstance<TransactionListItem.TransactionItem>()
        .map { it.date.id }

    @Test
    fun `starts loading and then shows transactions`() = runTest {
        val viewModel = viewModel()
        assertThat(viewModel.state.value.isLoading).isTrue()

        advanceUntilIdle()

        assertThat(viewModel.state.value.isLoading).isFalse()
        assertThat(viewModel.state.value.ids()).containsExactly("groceries", "rent")
    }

    private fun upcomingRent() = FAKE_EXPENSE_TRANSACTION.copy(
        id = ProcessRecurringTransactionsUseCase.occurrenceTransactionId("rule-1", 3),
        notes = "Rent",
        createdOn = java.util.Date(System.currentTimeMillis() + 10L * 24 * 60 * 60 * 1000),
    ).apply {
        category = FAKE_CATEGORY
        fromAccount = FAKE_ACCOUNT
    }

    private fun TransactionListState.uiItems() = transactionListItem
        .filterIsInstance<TransactionListItem.TransactionItem>()
        .map { it.date }

    @Test
    fun `planned recurring occurrences are listed and marked upcoming`() = runTest {
        val viewModel = viewModel(upcoming = listOf(upcomingRent()))
        advanceUntilIdle()

        val items = viewModel.state.value.uiItems()
        assertThat(items.map { it.id }).contains(upcomingRent().id)
        assertThat(items.single { it.id == upcomingRent().id }.isUpcoming).isTrue()
        assertThat(items.filter { it.id != upcomingRent().id }.none { it.isUpcoming }).isTrue()
    }

    @Test
    fun `an occurrence that is already logged is not shown again as upcoming`() = runTest {
        val logged = upcomingRent()
        val viewModel = viewModel(transactions = listOf(logged), upcoming = listOf(upcomingRent()))
        advanceUntilIdle()

        val items = viewModel.state.value.uiItems()
        assertThat(items).hasSize(1)
        assertThat(items.single().isUpcoming).isFalse()
    }

    @Test
    fun `tapping an upcoming occurrence opens its recurring rule`() = runTest {
        val viewModel = viewModel(upcoming = listOf(upcomingRent()))
        advanceUntilIdle()

        viewModel.processAction(TransactionListAction.OpenEdiTransaction(upcomingRent().id))

        verify(navigator).navigate(ExpenseManagerScreens.RecurringTransactionEdit("rule-1"))
    }

    @Test
    fun `tapping a real transaction still opens it for editing`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.processAction(TransactionListAction.OpenEdiTransaction("rent"))

        verify(navigator).navigate(ExpenseManagerScreens.TransactionCreate("rent"))
    }

    @Test
    fun `recurring icon opens the recurring transactions list`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.processAction(TransactionListAction.OpenRecurringTransactions)

        verify(navigator).navigate(ExpenseManagerScreens.RecurringTransactions)
    }

    @Test
    fun `search filters by notes ignoring case`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.processAction(TransactionListAction.Search("GROCER"))
        advanceUntilIdle()

        assertThat(viewModel.state.value.ids()).containsExactly("groceries")
        assertThat(viewModel.state.value.searchQuery).isEqualTo("GROCER")
    }

    @Test
    fun `undo re-adds the deleted transaction and keeps its attachments`() = runTest {
        whenever(deleteTransactionUseCase.invoke(groceries)).thenReturn(Resource.Success(true))
        whenever(addTransactionUseCase.invoke(groceries)).thenReturn(Resource.Success(true))
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.event.test {
            viewModel.processAction(TransactionListAction.DeleteTransaction("groceries"))
            assertThat(awaitItem()).isEqualTo(TransactionListEvent.ShowUndoDelete)
        }
        viewModel.processAction(TransactionListAction.UndoDelete)
        advanceUntilIdle()

        verify(addTransactionUseCase).invoke(groceries)
        verify(imageStorageRepository, never()).deleteTransactionAttachment(any())
    }

    @Test
    fun `attachments are removed once the undo window passes`() = runTest {
        whenever(deleteTransactionUseCase.invoke(groceries)).thenReturn(Resource.Success(true))
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.processAction(TransactionListAction.DeleteTransaction("groceries"))
        advanceUntilIdle()
        viewModel.processAction(TransactionListAction.UndoExpired)

        verify(imageStorageRepository).deleteTransactionAttachment("receipt.jpg")
        verify(addTransactionUseCase, never()).invoke(any())
    }
}
