package com.naveenapps.expensemanager.feature.transaction.recurring

import com.naveenapps.expensemanager.core.domain.usecase.recurring.DeleteRecurringTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.recurring.GetRecurringTransactionsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.RecurringFrequency
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RecurringTransactionsViewModelTest : BaseCoroutineTest() {

    private val getRecurringTransactionsUseCase: GetRecurringTransactionsUseCase = mock()
    private val getCurrencyUseCase: GetCurrencyUseCase = mock()
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase = mock()
    private val deleteRecurringTransactionUseCase: DeleteRecurringTransactionUseCase = mock()
    private val navigator: AppComposeNavigator = mock()
    private val analyticsRepository: AnalyticsRepository = mock()

    private fun viewModel(): RecurringTransactionsViewModel {
        whenever(getRecurringTransactionsUseCase.invoke()).thenReturn(flowOf(emptyList()))
        whenever(getCurrencyUseCase.invoke()).thenReturn(flowOf(Currency("$", "USD")))
        return RecurringTransactionsViewModel(
            getRecurringTransactionsUseCase = getRecurringTransactionsUseCase,
            getCurrencyUseCase = getCurrencyUseCase,
            getFormattedAmountUseCase = getFormattedAmountUseCase,
            deleteRecurringTransactionUseCase = deleteRecurringTransactionUseCase,
            appComposeNavigator = navigator,
            analyticsRepository = analyticsRepository,
        )
    }

    @Test
    fun `add opens create transaction with monthly repeat pre-selected`() {
        viewModel().processAction(RecurringTransactionsAction.OpenCreate)

        verify(navigator).navigate(
            ExpenseManagerScreens.TransactionCreate(id = null, repeat = RecurringFrequency.MONTHLY.name),
        )
    }
}
