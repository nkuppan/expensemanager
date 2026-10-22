package com.naveenapps.expensemanager.feature.transaction.recurring

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.recurring.DeleteRecurringTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.recurring.FindRecurringTransactionByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.recurring.RecurringTransactionChanges
import com.naveenapps.expensemanager.core.domain.usecase.recurring.UpdateRecurringTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.RecurringFrequency
import com.naveenapps.expensemanager.core.model.RecurringTransaction
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerArgsNames
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.feature.transaction.R
import com.naveenapps.expensemanager.feature.transaction.recurring.edit.RecurringTransactionEditAction
import com.naveenapps.expensemanager.feature.transaction.recurring.edit.RecurringTransactionEditEvent
import com.naveenapps.expensemanager.feature.transaction.recurring.edit.RecurringTransactionEditViewModel
import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RecurringTransactionEditViewModelTest : BaseCoroutineTest() {

    private val findRecurringTransactionByIdUseCase: FindRecurringTransactionByIdUseCase = mock()
    private val updateRecurringTransactionUseCase: UpdateRecurringTransactionUseCase = mock()
    private val deleteRecurringTransactionUseCase: DeleteRecurringTransactionUseCase = mock()
    private val getCurrencyUseCase: GetCurrencyUseCase = mock()
    private val getAllAccountsUseCase: GetAllAccountsUseCase = mock()
    private val getAllCategoryUseCase: GetAllCategoryUseCase = mock()
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase = mock()
    private val numberFormatRepository: NumberFormatRepository = mock()
    private val appComposeNavigator: AppComposeNavigator = mock()
    private val analyticsRepository: AnalyticsRepository = mock()

    private val currency = Currency(symbol = "$", name = "USD")
    private val nextDue = Date(1_800_000_000_000)

    private val rule = RecurringTransaction(
        id = "rule-1",
        notes = "Rent",
        categoryId = "rent",
        fromAccountId = "bank",
        toAccountId = null,
        type = TransactionType.EXPENSE,
        amount = 1000.0,
        frequency = RecurringFrequency.MONTHLY,
        startDate = Date(1_790_000_000_000),
        nextOccurrence = 1,
        nextDueDate = nextDue,
        createdOn = Date(),
        updatedOn = Date(),
    )

    private val rentCategory = Category(
        id = "rent",
        name = "Rent",
        type = CategoryType.EXPENSE,
        storedIcon = StoredIcon(name = "ic_home", backgroundColor = "#000000"),
        createdOn = Date(),
        updatedOn = Date(),
    )

    private val bank = Account(
        id = "bank",
        name = "Bank",
        type = AccountType.REGULAR,
        storedIcon = StoredIcon(name = "ic_wallet", backgroundColor = "#000000"),
        createdOn = Date(),
        updatedOn = Date(),
    )

    override fun onCreate() {
        super.onCreate()
        whenever(getCurrencyUseCase.invoke()).thenReturn(flowOf(currency))
        whenever(getAllAccountsUseCase.invoke()).thenReturn(flowOf(listOf(bank)))
        whenever(getAllCategoryUseCase.invoke()).thenReturn(flowOf(listOf(rentCategory)))
        whenever(getFormattedAmountUseCase.invoke(any(), any())).thenReturn(Amount(0.0))
        whenever(numberFormatRepository.formatForEditing(1000.0)).thenReturn("1000.00")
        whenever(numberFormatRepository.parseToDouble("1000.00")).thenReturn(1000.0)
        whenever(numberFormatRepository.parseToDouble("1200")).thenReturn(1200.0)
    }

    private suspend fun createLoadedViewModel(found: RecurringTransaction? = rule): RecurringTransactionEditViewModel {
        whenever(findRecurringTransactionByIdUseCase.invoke("rule-1")).thenReturn(found)
        return RecurringTransactionEditViewModel(
            savedStateHandle = SavedStateHandle(mapOf(ExpenseManagerArgsNames.ID to "rule-1")),
            findRecurringTransactionByIdUseCase = findRecurringTransactionByIdUseCase,
            updateRecurringTransactionUseCase = updateRecurringTransactionUseCase,
            deleteRecurringTransactionUseCase = deleteRecurringTransactionUseCase,
            getCurrencyUseCase = getCurrencyUseCase,
            getAllAccountsUseCase = getAllAccountsUseCase,
            getAllCategoryUseCase = getAllCategoryUseCase,
            getFormattedAmountUseCase = getFormattedAmountUseCase,
            numberFormatRepository = numberFormatRepository,
            appComposeNavigator = appComposeNavigator,
            analyticsRepository = analyticsRepository,
        )
    }

    @Test
    fun `loads the rule into the form`() = runTest {
        val viewModel = createLoadedViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.isLoaded).isTrue()
        assertThat(state.amount.value).isEqualTo("1000.00")
        assertThat(state.notes.value).isEqualTo("Rent")
        assertThat(state.frequency).isEqualTo(RecurringFrequency.MONTHLY)
        assertThat(state.nextDueDate).isEqualTo(nextDue)
        assertThat(state.selectedCategory?.id).isEqualTo("rent")
        assertThat(state.selectedFromAccount?.id).isEqualTo("bank")
        assertThat(state.isScheduleChanged).isFalse()
    }

    @Test
    fun `a rule that no longer exists closes the screen`() = runTest {
        createLoadedViewModel(found = null)
        advanceUntilIdle()

        verify(appComposeNavigator).popBackStack()
    }

    @Test
    fun `saving with nothing changed just closes`() = runTest {
        val viewModel = createLoadedViewModel()
        advanceUntilIdle()

        viewModel.processAction(RecurringTransactionEditAction.Save)
        advanceUntilIdle()

        verify(updateRecurringTransactionUseCase, never()).invoke(any(), any(), any())
        verify(appComposeNavigator).popBackStack()
    }

    @Test
    fun `saving an edited amount keeps the schedule`() = runTest {
        val viewModel = createLoadedViewModel()
        advanceUntilIdle()
        whenever(updateRecurringTransactionUseCase.invoke(eq("rule-1"), any(), any()))
            .thenReturn(Resource.Success(rule.copy(amount = 1200.0)))

        viewModel.state.value.amount.onValueChange?.invoke("1200")
        viewModel.processAction(RecurringTransactionEditAction.Save)
        advanceUntilIdle()

        val captor = argumentCaptor<RecurringTransactionChanges>()
        verify(updateRecurringTransactionUseCase).invoke(eq("rule-1"), captor.capture(), any())
        assertThat(captor.firstValue.amount).isEqualTo(1200.0)
        assertThat(captor.firstValue.frequency).isEqualTo(RecurringFrequency.MONTHLY)
        assertThat(captor.firstValue.nextDueDate).isEqualTo(nextDue)
        verify(appComposeNavigator).popBackStack()
    }

    @Test
    fun `changing the frequency is flagged as a schedule change`() = runTest {
        val viewModel = createLoadedViewModel()
        advanceUntilIdle()

        viewModel.processAction(RecurringTransactionEditAction.SelectFrequency(RecurringFrequency.WEEKLY))
        assertThat(viewModel.state.value.isScheduleChanged).isTrue()
        assertThat(viewModel.hasUnsavedChanges()).isTrue()

        viewModel.processAction(RecurringTransactionEditAction.SelectFrequency(RecurringFrequency.MONTHLY))
        assertThat(viewModel.state.value.isScheduleChanged).isFalse()
    }

    @Test
    fun `picking the same day again is not a schedule change`() = runTest {
        val viewModel = createLoadedViewModel()
        advanceUntilIdle()

        viewModel.processAction(RecurringTransactionEditAction.SelectDate(nextDue))

        assertThat(viewModel.state.value.isScheduleChanged).isFalse()
    }

    @Test
    fun `a rejected past next date shows a message and stays open`() = runTest {
        val viewModel = createLoadedViewModel()
        advanceUntilIdle()
        whenever(updateRecurringTransactionUseCase.invoke(eq("rule-1"), any(), any()))
            .thenReturn(Resource.Error(IllegalArgumentException("past")))

        viewModel.event.test {
            viewModel.processAction(RecurringTransactionEditAction.SelectFrequency(RecurringFrequency.WEEKLY))
            viewModel.processAction(RecurringTransactionEditAction.Save)

            assertThat(awaitItem()).isEqualTo(
                RecurringTransactionEditEvent.ShowMessage(R.string.recurring_next_date_in_past),
            )
        }
        verify(appComposeNavigator, never()).popBackStack()
    }

    @Test
    fun `stopping deletes the rule and closes`() = runTest {
        val viewModel = createLoadedViewModel()
        advanceUntilIdle()

        viewModel.processAction(RecurringTransactionEditAction.ShowStopDialog)
        viewModel.processAction(RecurringTransactionEditAction.ConfirmStop)
        advanceUntilIdle()

        verify(deleteRecurringTransactionUseCase).invoke("rule-1")
        verify(appComposeNavigator).popBackStack()
    }
}
