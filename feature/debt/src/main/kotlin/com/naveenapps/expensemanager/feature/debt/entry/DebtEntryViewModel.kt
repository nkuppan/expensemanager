package com.naveenapps.expensemanager.feature.debt.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.debt.DebtLedger
import com.naveenapps.expensemanager.core.domain.usecase.debt.SaveDebtEntryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.DebtEntryKind
import com.naveenapps.expensemanager.core.model.DebtPerson
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.core.model.getAvailableCreditLimit
import com.naveenapps.expensemanager.core.model.milliToDouble
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.core.model.toMilli
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerArgsNames
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.DebtRepository
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import com.naveenapps.expensemanager.core.repository.logFailure
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatRepository
import com.naveenapps.expensemanager.feature.debt.R
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DebtEntryState(
    val isLoaded: Boolean = false,
    val isEditing: Boolean = false,
    val kind: DebtEntryKind = DebtEntryKind.LENT,
    /** Fixed when opened for a person or editing; otherwise the user types or picks one. */
    val fixedPersonName: String? = null,
    val personName: TextFieldValue<String>,
    val people: List<DebtPerson> = emptyList(),
    val amount: TextFieldValue<String>,
    val note: TextFieldValue<String>,
    val linkAccount: Boolean = true,
    val accounts: List<AccountUiModel> = emptyList(),
    val selectedAccount: AccountUiModel? = null,
    val date: Instant = Clock.System.now(),
    val dueDate: Instant? = null,
    val showNumberPad: Boolean = false,
    val numberPadInitialAmount: Double? = null,
    val showAccountSelection: Boolean = false,
    val showDateSelection: Boolean = false,
    val showDueDateSelection: Boolean = false,
    val showDeleteDialog: Boolean = false,
)

sealed class DebtEntryAction {
    data object ClosePage : DebtEntryAction()
    data class SelectKind(val kind: DebtEntryKind) : DebtEntryAction()
    data class PickPerson(val person: DebtPerson) : DebtEntryAction()
    data object ShowNumberPad : DebtEntryAction()
    data class SetNumberPadValue(val amount: String?) : DebtEntryAction()
    data class SetLinkAccount(val link: Boolean) : DebtEntryAction()
    data object ShowAccountSelection : DebtEntryAction()
    data object DismissAccountSelection : DebtEntryAction()
    data class SelectAccount(val account: AccountUiModel) : DebtEntryAction()
    data object ShowDateSelection : DebtEntryAction()
    data object ShowDueDateSelection : DebtEntryAction()
    data object DismissDateSelection : DebtEntryAction()
    data class SelectDate(val date: Instant) : DebtEntryAction()
    data class SelectDueDate(val date: Instant) : DebtEntryAction()
    data object ClearDueDate : DebtEntryAction()
    data object Save : DebtEntryAction()
    data object ShowDelete : DebtEntryAction()
    data object DismissDelete : DebtEntryAction()
    data object ConfirmDelete : DebtEntryAction()
}

sealed class DebtEntryEvent {
    data class ShowMessage(val messageRes: Int) : DebtEntryEvent()
}

/**
 * Adds or edits one debt entry: who, what kind, how much, the account it moved money in (if
 * any), when, an optional due date and a note. "Settle up" opens it filled with the person's
 * whole open balance as a repayment.
 */
class DebtEntryViewModel(
    savedStateHandle: SavedStateHandle,
    private val debtRepository: DebtRepository,
    private val saveDebtEntryUseCase: SaveDebtEntryUseCase,
    getAllAccountsUseCase: GetAllAccountsUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val numberFormatRepository: NumberFormatRepository,
    private val settingsRepository: SettingsRepository,
    private val appComposeNavigator: AppComposeNavigator,
    private val analyticsRepository: AnalyticsRepository,
) : ViewModel() {

    private val entryId: String? = savedStateHandle.get<String>(ExpenseManagerArgsNames.ID)
    private var personId: String? = savedStateHandle.get<String>(ExpenseManagerArgsNames.PERSON_ID)
    private val settle: Boolean = savedStateHandle.get<Boolean>(ExpenseManagerArgsNames.SETTLE) == true
    private val initialKind: DebtEntryKind = savedStateHandle.get<String>(ExpenseManagerArgsNames.KIND)
        ?.let { name -> DebtEntryKind.entries.find { it.name == name } }
        ?: DebtEntryKind.LENT

    // The account stored on the entry being edited, picked once the account list loads.
    private var storedAccountId: String? = null

    private val _state = MutableStateFlow(
        DebtEntryState(
            kind = initialKind,
            personName = TextFieldValue(value = "", onValueChange = this::setPersonName),
            amount = TextFieldValue(value = "", onValueChange = this::setAmount),
            note = TextFieldValue(value = "", onValueChange = this::setNote),
        ),
    )
    val state = _state.asStateFlow()

    private val _event = Channel<DebtEntryEvent>()
    val event = _event.receiveAsFlow()

    init {
        viewModelScope.launch { load() }

        combine(getAllAccountsUseCase(), getCurrencyUseCase()) { accounts, currency ->
            accounts.map { account ->
                account.toAccountUiModel(
                    getFormattedAmountUseCase(account.amount, currency),
                    if (account.type == AccountType.CREDIT) {
                        getFormattedAmountUseCase(account.getAvailableCreditLimit(), currency)
                    } else {
                        null
                    },
                )
            }
        }.onEach { accounts ->
            val defaultId = settingsRepository.getDefaultAccount().firstOrNull()
            _state.update { current ->
                val wantedId = current.selectedAccount?.id ?: storedAccountId ?: defaultId
                current.copy(
                    accounts = accounts,
                    selectedAccount = accounts.find { it.id == wantedId } ?: accounts.firstOrNull(),
                )
            }
        }.launchIn(viewModelScope)

        debtRepository.getPeople()
            .onEach { people -> _state.update { it.copy(people = people) } }
            .launchIn(viewModelScope)
    }

    private suspend fun load() {
        val editing = entryId?.let { debtRepository.findEntry(it) }
        if (editing != null) {
            personId = editing.personId
            storedAccountId = editing.accountId
            val person = debtRepository.findPerson(editing.personId)
            _state.update {
                it.copy(
                    isEditing = true,
                    kind = editing.kind,
                    fixedPersonName = person?.name,
                    amount = it.amount.copy(
                        value = numberFormatRepository.formatForEditing(editing.amountMilli.milliToDouble()),
                    ),
                    note = it.note.copy(value = editing.note),
                    linkAccount = editing.accountId != null,
                    date = editing.date,
                    dueDate = editing.dueDate,
                    isLoaded = true,
                )
            }
            return
        }
        if (entryId != null) {
            // The entry was deleted elsewhere.
            appComposeNavigator.popBackStack()
            return
        }
        val person = personId?.let { debtRepository.findPerson(it) }
        if (settle && person != null) {
            val balance = DebtLedger.balanceOf(debtRepository.getAllEntries().filter { it.personId == person.id })
            _state.update {
                it.copy(
                    kind = if (balance >= 0) DebtEntryKind.RECEIVED else DebtEntryKind.PAID,
                    amount = it.amount.copy(
                        value = if (balance == 0L) {
                            ""
                        } else {
                            numberFormatRepository.formatForEditing(abs(balance).milliToDouble())
                        },
                    ),
                )
            }
        }
        _state.update {
            it.copy(
                fixedPersonName = person?.name,
                isLoaded = true,
                // A new entry opens straight on the keypad, like adding a transaction.
                showNumberPad = !settle,
            )
        }
    }

    private fun setPersonName(name: String) {
        // Typing a different name forgets a person picked from the suggestions.
        if (_state.value.fixedPersonName == null) personId = null
        _state.update { it.copy(personName = it.personName.copy(value = name, valueError = false)) }
    }

    private fun setAmount(amount: String) {
        _state.update { it.copy(amount = it.amount.copy(value = amount, valueError = false)) }
    }

    private fun setNote(note: String) {
        _state.update { it.copy(note = it.note.copy(value = note)) }
    }

    fun processAction(action: DebtEntryAction) {
        when (action) {
            DebtEntryAction.ClosePage -> appComposeNavigator.popBackStack()

            is DebtEntryAction.SelectKind -> _state.update {
                it.copy(kind = action.kind, dueDate = it.dueDate.takeIf { action.kind.isNewDebt })
            }

            is DebtEntryAction.PickPerson -> {
                personId = action.person.id
                _state.update {
                    it.copy(personName = it.personName.copy(value = action.person.name, valueError = false))
                }
            }

            DebtEntryAction.ShowNumberPad -> _state.update {
                it.copy(
                    showNumberPad = true,
                    numberPadInitialAmount = numberFormatRepository.parseToDouble(it.amount.value),
                )
            }

            is DebtEntryAction.SetNumberPadValue -> {
                action.amount?.let(::setAmount)
                _state.update { it.copy(showNumberPad = false) }
            }

            is DebtEntryAction.SetLinkAccount -> _state.update { it.copy(linkAccount = action.link) }

            DebtEntryAction.ShowAccountSelection -> _state.update { it.copy(showAccountSelection = true) }

            DebtEntryAction.DismissAccountSelection -> _state.update { it.copy(showAccountSelection = false) }

            is DebtEntryAction.SelectAccount -> _state.update {
                it.copy(selectedAccount = action.account, showAccountSelection = false)
            }

            DebtEntryAction.ShowDateSelection -> _state.update { it.copy(showDateSelection = true) }

            DebtEntryAction.ShowDueDateSelection -> _state.update { it.copy(showDueDateSelection = true) }

            DebtEntryAction.DismissDateSelection -> _state.update {
                it.copy(showDateSelection = false, showDueDateSelection = false)
            }

            is DebtEntryAction.SelectDate -> _state.update { it.copy(date = action.date, showDateSelection = false) }

            is DebtEntryAction.SelectDueDate -> _state.update {
                it.copy(dueDate = action.date, showDueDateSelection = false)
            }

            DebtEntryAction.ClearDueDate -> _state.update { it.copy(dueDate = null) }

            DebtEntryAction.Save -> save()

            DebtEntryAction.ShowDelete -> _state.update { it.copy(showDeleteDialog = true) }

            DebtEntryAction.DismissDelete -> _state.update { it.copy(showDeleteDialog = false) }

            DebtEntryAction.ConfirmDelete -> delete()
        }
    }

    private fun save() {
        val current = _state.value
        val amount = numberFormatRepository.parseToDouble(current.amount.value)
        if (amount == null || amount <= 0.0) {
            _state.update { it.copy(amount = it.amount.copy(valueError = true)) }
            return
        }
        val typedName = current.personName.value.trim()
        // A typed name that matches someone already in the list is that person.
        val matchedId = personId ?: current.people.find { it.name.equals(typedName, ignoreCase = true) }?.id
        if (matchedId == null && typedName.isEmpty()) {
            _state.update { it.copy(personName = it.personName.copy(valueError = true)) }
            return
        }
        val accountId = current.selectedAccount?.id.takeIf { current.linkAccount }
        viewModelScope.launch {
            val result = saveDebtEntryUseCase(
                entryId = entryId,
                personId = matchedId,
                personName = typedName,
                kind = current.kind,
                amountMilli = amount.toMilli(),
                accountId = accountId,
                date = current.date,
                dueDate = current.dueDate,
                note = current.note.value,
            )
            when (result) {
                is Resource.Success -> {
                    analyticsRepository.logEvent(
                        AnalyticsEvents.DEBT_ENTRY_SAVED,
                        mapOf(
                            AnalyticsParams.TYPE to current.kind.name.lowercase(),
                            AnalyticsParams.METHOD to when {
                                current.isEditing -> METHOD_EDIT
                                settle -> METHOD_SETTLE
                                else -> METHOD_NEW
                            },
                            AnalyticsParams.LINKED_ACCOUNT to (accountId != null).toString(),
                        ),
                    )
                    appComposeNavigator.popBackStack()
                }

                is Resource.Error -> {
                    analyticsRepository.logFailure(FAILED_SAVE, result.exception)
                    _event.send(DebtEntryEvent.ShowMessage(R.string.debt_save_failed))
                }
            }
        }
    }

    private fun delete() {
        val id = entryId ?: return
        _state.update { it.copy(showDeleteDialog = false) }
        viewModelScope.launch {
            val kind = _state.value.kind
            runCatching { debtRepository.deleteEntry(id) }
                .onSuccess {
                    analyticsRepository.logEvent(
                        AnalyticsEvents.DEBT_ENTRY_DELETED,
                        mapOf(AnalyticsParams.TYPE to kind.name.lowercase()),
                    )
                    appComposeNavigator.popBackStack()
                }
                .onFailure { error ->
                    analyticsRepository.logFailure(FAILED_DELETE, error)
                    _event.send(DebtEntryEvent.ShowMessage(R.string.debt_save_failed))
                }
        }
    }

    private companion object {
        const val METHOD_NEW = "new"
        const val METHOD_EDIT = "edit"
        const val METHOD_SETTLE = "settle"
        const val FAILED_SAVE = "debt_save"
        const val FAILED_DELETE = "debt_delete"
    }
}
