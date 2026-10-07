package com.naveenapps.expensemanager.feature.transaction.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.recurring.DeleteRecurringTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.recurring.GetRecurringTransactionsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.RecurringFrequency
import com.naveenapps.expensemanager.core.model.RecurringTransaction
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecurringTransactionsState(
    /** Null until loaded, so the empty state doesn't flash in. */
    val items: List<RecurringItemUi>? = null,
    val pendingStop: RecurringItemUi? = null,
)

data class RecurringItemUi(
    val rule: RecurringTransaction,
    val amount: String,
)

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

    init {
        combine(getRecurringTransactionsUseCase.invoke(), getCurrencyUseCase.invoke()) { rules, currency ->
            rules.map { RecurringItemUi(it, getFormattedAmountUseCase.invoke(it.amount, currency).amountString.orEmpty()) }
        }.onEach { items -> _state.update { it.copy(items = items) } }.launchIn(viewModelScope)
    }

    fun askToStop(item: RecurringItemUi) = _state.update { it.copy(pendingStop = item) }

    fun dismissStop() = _state.update { it.copy(pendingStop = null) }

    fun confirmStop() {
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

    fun closePage() = appComposeNavigator.popBackStack()
}

internal val RecurringFrequency.labelResId: Int
    get() = when (this) {
        RecurringFrequency.DAILY -> com.naveenapps.expensemanager.feature.transaction.R.string.repeat_daily
        RecurringFrequency.WEEKLY -> com.naveenapps.expensemanager.feature.transaction.R.string.repeat_weekly
        RecurringFrequency.MONTHLY -> com.naveenapps.expensemanager.feature.transaction.R.string.repeat_monthly
        RecurringFrequency.YEARLY -> com.naveenapps.expensemanager.feature.transaction.R.string.repeat_yearly
    }
