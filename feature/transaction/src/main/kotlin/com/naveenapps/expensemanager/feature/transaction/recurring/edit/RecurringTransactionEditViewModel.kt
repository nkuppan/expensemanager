package com.naveenapps.expensemanager.feature.transaction.recurring.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.designsystem.R as DesignSystemR
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.recurring.DeleteRecurringTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.recurring.FindRecurringTransactionByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.recurring.RecurringTransactionChanges
import com.naveenapps.expensemanager.core.domain.usecase.recurring.UpdateRecurringTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.RecurringTransaction
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.getAvailableCreditLimit
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.model.isIncome
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerArgsNames
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatRepository
import com.naveenapps.expensemanager.feature.transaction.R
import com.naveenapps.expensemanager.feature.transaction.create.AccountSelection
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Edits a repeating transaction rule. The rule is the parent of its occurrences: saving here
 * only changes occurrences that haven't been logged yet (see UpdateRecurringTransactionUseCase).
 */
class RecurringTransactionEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val findRecurringTransactionByIdUseCase: FindRecurringTransactionByIdUseCase,
    private val updateRecurringTransactionUseCase: UpdateRecurringTransactionUseCase,
    private val deleteRecurringTransactionUseCase: DeleteRecurringTransactionUseCase,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getAllAccountsUseCase: GetAllAccountsUseCase,
    private val getAllCategoryUseCase: GetAllCategoryUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val numberFormatRepository: NumberFormatRepository,
    private val appComposeNavigator: AppComposeNavigator,
    private val analyticsRepository: AnalyticsRepository,
) : ViewModel() {

    private val ruleId: String? = savedStateHandle.get<String>(ExpenseManagerArgsNames.ID)

    private val _state = MutableStateFlow(
        RecurringTransactionEditState(
            amount = TextFieldValue(value = "", valueError = false, onValueChange = this::setAmount),
            notes = TextFieldValue(value = "", valueError = false, onValueChange = this::setNotes),
        ),
    )
    val state = _state.asStateFlow()

    private val _event = Channel<RecurringTransactionEditEvent>()
    val event = _event.receiveAsFlow()

    /** The rule as stored when the screen opened; null until loaded. */
    private var original: RecurringTransaction? = null

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val rule = ruleId?.let { findRecurringTransactionByIdUseCase.invoke(it) }
        if (rule == null) {
            // Deleted elsewhere (e.g. its account or category was removed) or a bad id.
            appComposeNavigator.popBackStack()
            return
        }
        original = rule
        _state.update {
            it.copy(
                amount = it.amount.copy(value = numberFormatRepository.formatForEditing(rule.amount)),
                notes = it.notes.copy(value = rule.notes),
                transactionType = rule.type,
                frequency = rule.frequency,
                nextDueDate = rule.nextDueDate,
            )
        }

        combine(
            getCurrencyUseCase.invoke(),
            getAllAccountsUseCase.invoke(),
            getAllCategoryUseCase.invoke(),
        ) { currency, accounts, categories ->
            val accountModels = accounts.map { account ->
                account.toAccountUiModel(
                    getFormattedAmountUseCase.invoke(account.amount, currency),
                    if (account.type == AccountType.CREDIT) {
                        getFormattedAmountUseCase.invoke(account.getAvailableCreditLimit(), currency)
                    } else {
                        null
                    },
                )
            }
            val typeCategories = categories.filter { category ->
                if (rule.type.isIncome()) category.type.isIncome() else category.type.isExpense()
            }
            _state.update { current ->
                // Keep the user's current pick (refreshed from the new lists); fall back to the
                // rule's stored ids on first load.
                val categoryId = current.selectedCategory?.id ?: rule.categoryId
                val fromId = current.selectedFromAccount?.id ?: rule.fromAccountId
                val toId = current.selectedToAccount?.id ?: rule.toAccountId
                current.copy(
                    isLoaded = true,
                    accounts = accountModels,
                    categories = typeCategories,
                    // A transfer's category isn't in the type-filtered list; keep it from the full list.
                    selectedCategory = categories.find { it.id == categoryId },
                    selectedFromAccount = accountModels.find { it.id == fromId },
                    selectedToAccount = toId?.let { id -> accountModels.find { it.id == id } },
                )
            }
        }.launchIn(viewModelScope)
    }

    fun processAction(action: RecurringTransactionEditAction) {
        when (action) {
            RecurringTransactionEditAction.ClosePage -> requestClose()

            RecurringTransactionEditAction.ConfirmDiscard -> {
                _state.update { it.copy(showDiscardDialog = false) }
                appComposeNavigator.popBackStack()
            }

            RecurringTransactionEditAction.DismissDiscardDialog -> _state.update { it.copy(showDiscardDialog = false) }

            RecurringTransactionEditAction.Save -> save()

            RecurringTransactionEditAction.ShowStopDialog -> _state.update { it.copy(showStopDialog = true) }

            RecurringTransactionEditAction.DismissStopDialog -> _state.update { it.copy(showStopDialog = false) }

            RecurringTransactionEditAction.ConfirmStop -> stop()

            RecurringTransactionEditAction.ShowNumberPad -> _state.update {
                it.copy(
                    showNumberPad = true,
                    numberPadInitialAmount = numberFormatRepository.parseToDouble(it.amount.value),
                )
            }

            is RecurringTransactionEditAction.SetNumberPadValue -> action.amount?.let { setAmount(it) }
                ?: _state.update { it.copy(showNumberPad = false) }

            is RecurringTransactionEditAction.SelectFrequency -> updateSchedule { it.copy(frequency = action.frequency) }

            RecurringTransactionEditAction.ShowDateSelection -> _state.update { it.copy(showDateSelection = true) }

            RecurringTransactionEditAction.ShowTimeSelection -> _state.update { it.copy(showTimeSelection = true) }

            RecurringTransactionEditAction.DismissDateSelection -> _state.update {
                it.copy(showDateSelection = false, showTimeSelection = false)
            }

            is RecurringTransactionEditAction.SelectDate -> updateSchedule {
                it.copy(
                    nextDueDate = it.nextDueDate.withDayOf(action.date),
                    showDateSelection = false,
                )
            }

            is RecurringTransactionEditAction.SelectTime -> updateSchedule {
                it.copy(
                    nextDueDate = it.nextDueDate.withTime(action.hour, action.minute),
                    showTimeSelection = false,
                )
            }

            RecurringTransactionEditAction.ShowCategorySelection -> _state.update { it.copy(showCategorySelection = true) }

            RecurringTransactionEditAction.DismissCategorySelection -> _state.update { it.copy(showCategorySelection = false) }

            is RecurringTransactionEditAction.SelectCategory -> _state.update {
                it.copy(selectedCategory = action.category, showCategorySelection = false)
            }

            is RecurringTransactionEditAction.ShowAccountSelection -> _state.update {
                it.copy(showAccountSelection = true, accountSelection = action.type)
            }

            RecurringTransactionEditAction.DismissAccountSelection -> _state.update { it.copy(showAccountSelection = false) }

            is RecurringTransactionEditAction.SelectAccount -> _state.update {
                when (it.accountSelection) {
                    AccountSelection.FROM_ACCOUNT -> it.copy(selectedFromAccount = action.account, showAccountSelection = false)
                    AccountSelection.TO_ACCOUNT -> it.copy(selectedToAccount = action.account, showAccountSelection = false)
                }
            }
        }
    }

    // region Editing

    private fun setAmount(amount: String) {
        val value = numberFormatRepository.parseToDouble(amount)
        _state.update {
            it.copy(
                amount = it.amount.copy(value = amount, valueError = value == null || value <= 0.0),
                showNumberPad = false,
            )
        }
    }

    private fun setNotes(notes: String) {
        _state.update { it.copy(notes = it.notes.copy(value = notes)) }
    }

    /** Applies a frequency/date change and recomputes whether the schedule now differs. */
    private fun updateSchedule(change: (RecurringTransactionEditState) -> RecurringTransactionEditState) {
        _state.update { current ->
            val updated = change(current)
            val rule = original
            updated.copy(
                isScheduleChanged = rule != null &&
                    (updated.frequency != rule.frequency || updated.nextDueDate.time != rule.nextDueDate.time),
            )
        }
    }

    // endregion

    // region Save / stop / close

    private fun save() {
        val rule = original ?: return
        val current = _state.value
        val amount = numberFormatRepository.parseToDouble(current.amount.value)
        if (amount == null || amount <= 0.0) {
            _state.update { it.copy(amount = it.amount.copy(valueError = true)) }
            return
        }
        val category = current.selectedCategory
        val from = current.selectedFromAccount
        val to = current.selectedToAccount
        if (category == null || from == null) return
        val isTransfer = current.transactionType == TransactionType.TRANSFER
        if (isTransfer && (to == null || to.id == from.id)) {
            sendMessage(R.string.select_different_account)
            return
        }
        if (!hasUnsavedChanges()) {
            appComposeNavigator.popBackStack()
            return
        }

        viewModelScope.launch {
            val result = updateRecurringTransactionUseCase.invoke(
                id = rule.id,
                changes = RecurringTransactionChanges(
                    amount = amount,
                    notes = current.notes.value,
                    categoryId = category.id,
                    fromAccountId = from.id,
                    toAccountId = if (isTransfer) to?.id else null,
                    frequency = current.frequency,
                    nextDueDate = current.nextDueDate,
                ),
            )
            when (result) {
                is Resource.Success -> {
                    analyticsRepository.logEvent(
                        AnalyticsEvents.RECURRING_EDITED,
                        mapOf(AnalyticsParams.FREQUENCY to current.frequency.name.lowercase()),
                    )
                    appComposeNavigator.popBackStack()
                }

                is Resource.Error -> sendMessage(
                    if (result.exception is IllegalArgumentException) {
                        R.string.recurring_next_date_in_past
                    } else {
                        DesignSystemR.string.error_save_failed
                    },
                )
            }
        }
    }

    private fun stop() {
        val rule = original ?: return
        _state.update { it.copy(showStopDialog = false) }
        viewModelScope.launch {
            deleteRecurringTransactionUseCase.invoke(rule.id)
            analyticsRepository.logEvent(
                AnalyticsEvents.RECURRING_STOPPED,
                mapOf(AnalyticsParams.FREQUENCY to rule.frequency.name.lowercase()),
            )
            appComposeNavigator.popBackStack()
        }
    }

    private fun requestClose() {
        if (hasUnsavedChanges()) {
            _state.update { it.copy(showDiscardDialog = true) }
        } else {
            appComposeNavigator.popBackStack()
        }
    }

    internal fun hasUnsavedChanges(state: RecurringTransactionEditState = _state.value): Boolean {
        val rule = original ?: return false
        val amount = numberFormatRepository.parseToDouble(state.amount.value)
        // Round-trip through the edit formatter so precision beyond the currency's decimals
        // doesn't count as a change.
        val originalAmount = numberFormatRepository.parseToDouble(
            numberFormatRepository.formatForEditing(rule.amount),
        ) ?: rule.amount
        val toChanged = state.transactionType == TransactionType.TRANSFER &&
            state.selectedToAccount?.id != rule.toAccountId
        return amount != originalAmount ||
            state.notes.value != rule.notes ||
            state.selectedCategory?.id != rule.categoryId ||
            state.selectedFromAccount?.id != rule.fromAccountId ||
            toChanged ||
            state.isScheduleChanged
    }

    private fun sendMessage(messageRes: Int) {
        viewModelScope.launch { _event.send(RecurringTransactionEditEvent.ShowMessage(messageRes)) }
    }

    // endregion
}

/**
 * This date's exact time of day (down to the millisecond) on [day]'s calendar date, so picking
 * the same day again doesn't register as a schedule change.
 */
internal fun Date.withDayOf(day: Date): Date {
    val source = Calendar.getInstance().apply { time = this@withDayOf }
    val target = Calendar.getInstance().apply { time = day }
    return (source.clone() as Calendar).apply {
        set(target.get(Calendar.YEAR), target.get(Calendar.MONTH), target.get(Calendar.DAY_OF_MONTH))
    }.time
}

/** Same day at [hour]:[minute]; unchanged (seconds included) when the time wasn't changed. */
internal fun Date.withTime(hour: Int, minute: Int): Date {
    val calendar = Calendar.getInstance().apply { time = this@withTime }
    if (calendar.get(Calendar.HOUR_OF_DAY) == hour && calendar.get(Calendar.MINUTE) == minute) return this
    return calendar.apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time
}
