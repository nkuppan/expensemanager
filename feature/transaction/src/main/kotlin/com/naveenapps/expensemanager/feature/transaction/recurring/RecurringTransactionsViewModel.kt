package com.naveenapps.expensemanager.feature.transaction.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.recurring.DeleteRecurringTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.recurring.GetRecurringTransactionsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.RecurringFrequency
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.feature.transaction.R
import com.naveenapps.expensemanager.feature.transaction.search.matchesSearch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecurringTransactionsViewModel(
    getRecurringTransactionsUseCase: GetRecurringTransactionsUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val deleteRecurringTransactionUseCase: DeleteRecurringTransactionUseCase,
    private val appComposeNavigator: AppComposeNavigator,
    private val analyticsRepository: AnalyticsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RecurringTransactionsState())
    val state = _state.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        combine(
            getRecurringTransactionsUseCase.invoke(),
            getCurrencyUseCase.invoke(),
            searchQuery,
        ) { rules, currency, query ->
            val all = rules.map {
                RecurringItemUi(it, getFormattedAmountUseCase.invoke(it.amount, currency).amountString.orEmpty())
            }
            all.filter { it.matches(query) } to all.isNotEmpty()
        }.onEach { (items, hasAny) ->
            _state.update { it.copy(items = items, hasAnyRules = hasAny) }
        }.launchIn(viewModelScope)
    }

    fun processAction(action: RecurringTransactionsAction) {
        when (action) {
            RecurringTransactionsAction.ClosePage -> appComposeNavigator.popBackStack()

            is RecurringTransactionsAction.Search -> search(action.query)

            RecurringTransactionsAction.OpenCreate -> openCreate()

            is RecurringTransactionsAction.OpenEdit -> appComposeNavigator.navigate(
                ExpenseManagerScreens.RecurringTransactionEdit(action.item.rule.id),
            )

            is RecurringTransactionsAction.AskToStop -> _state.update { it.copy(pendingStop = action.item) }

            RecurringTransactionsAction.DismissStop -> _state.update { it.copy(pendingStop = null) }

            RecurringTransactionsAction.ConfirmStop -> confirmStop()
        }
    }

    // A recurring transaction is created like any other, with Repeat already chosen; the
    // first occurrence is the transaction itself. Monthly is the most common (rent, bills, salary).
    private fun openCreate() {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.TransactionCreate(id = null, repeat = RecurringFrequency.MONTHLY.name),
        )
    }

    private fun search(query: String) {
        searchQuery.value = query
        _state.update { it.copy(searchQuery = query) }
    }

    private fun confirmStop() {
        val item = _state.value.pendingStop ?: return
        _state.update { it.copy(pendingStop = null) }
        viewModelScope.launch {
            deleteRecurringTransactionUseCase.invoke(item.rule.id)
            analyticsRepository.logEvent(
                AnalyticsEvents.RECURRING_STOPPED,
                mapOf(AnalyticsParams.FREQUENCY to item.rule.frequency.name.lowercase()),
            )
        }
    }
}

/**
 * Notes, category name, account names and the amount (formatted, e.g. "1,200.00", or raw, e.g.
 * "1200"). Like the transaction list, built-in categories match by their stored English name.
 */
internal fun RecurringItemUi.matches(query: String): Boolean = matchesSearch(
    query,
    listOf(
        rule.notes,
        rule.category?.name,
        rule.fromAccount?.name,
        rule.toAccount?.name,
        amount,
        rule.amount.toBigDecimal().stripTrailingZeros().toPlainString(),
    ),
)

internal val RecurringFrequency.labelResId: Int
    get() = when (this) {
        RecurringFrequency.DAILY -> R.string.repeat_daily
        RecurringFrequency.WEEKLY -> R.string.repeat_weekly
        RecurringFrequency.MONTHLY -> R.string.repeat_monthly
        RecurringFrequency.YEARLY -> R.string.repeat_yearly
    }
