package com.naveenapps.expensemanager.feature.debt.person

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.debt.DebtLedger
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.DebtEntryKind
import com.naveenapps.expensemanager.core.model.milliToDouble
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerArgsNames
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.DebtRepository
import com.naveenapps.expensemanager.feature.debt.DebtStatus
import com.naveenapps.expensemanager.feature.debt.debtStatusOf
import com.naveenapps.expensemanager.feature.debt.isOverdue
import java.util.Date
import kotlin.math.abs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DebtEntryRow(
    val id: String,
    val kind: DebtEntryKind,
    val amount: String,
    val date: Date,
    val accountName: String?,
    val note: String,
    val dueDate: Date?,
)

data class DebtPersonState(
    val isLoaded: Boolean = false,
    val name: String = "",
    val status: DebtStatus = DebtStatus.SETTLED,
    /** Open balance, without a sign. */
    val balance: String = "",
    val dueDate: Date? = null,
    val isOverdue: Boolean = false,
    /** When the oldest debt still open started, for the reminder message. */
    val openSince: Date? = null,
    val entries: List<DebtEntryRow> = emptyList(),
    val showRenameDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
)

sealed class DebtPersonAction {
    data object ClosePage : DebtPersonAction()
    data class AddEntry(val kind: DebtEntryKind) : DebtPersonAction()
    data object SettleUp : DebtPersonAction()
    data class OpenEntry(val id: String) : DebtPersonAction()
    data object ReminderShared : DebtPersonAction()
    data object ShowRename : DebtPersonAction()
    data object DismissRename : DebtPersonAction()
    data class Rename(val name: String) : DebtPersonAction()
    data object ShowDelete : DebtPersonAction()
    data object DismissDelete : DebtPersonAction()
    data object ConfirmDelete : DebtPersonAction()
}

/** One person's page: their balance, every entry, and settle up / remind / rename / delete. */
class DebtPersonViewModel(
    savedStateHandle: SavedStateHandle,
    private val debtRepository: DebtRepository,
    getAllAccountsUseCase: GetAllAccountsUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val appComposeNavigator: AppComposeNavigator,
    private val analyticsRepository: AnalyticsRepository,
) : ViewModel() {

    private val personId: String = savedStateHandle.get<String>(ExpenseManagerArgsNames.ID).orEmpty()

    private var closed = false

    private val _state = MutableStateFlow(DebtPersonState())
    val state = _state.asStateFlow()

    init {
        val person = debtRepository.getPeople().map { people -> people.find { it.id == personId } }
        combine(
            person,
            debtRepository.getEntries(personId),
            getAllAccountsUseCase(),
            getCurrencyUseCase(),
        ) { found, entries, accounts, currency ->
            if (found == null) {
                // Deleted (here or elsewhere): nothing left to show. Close once only.
                if (!closed) {
                    closed = true
                    appComposeNavigator.popBackStack()
                }
                return@combine null
            }
            fun format(milli: Long): String = getFormattedAmountUseCase(abs(milli).milliToDouble(), currency).amountString.orEmpty()
            val balance = DebtLedger.balanceOf(entries)
            val dueDate = DebtLedger.dueDateOf(entries, balance)
            val openSign = if (balance > 0) 1 else -1
            val accountNames = accounts.associate { it.id to it.name }
            _state.value.copy(
                isLoaded = true,
                name = found.name,
                status = debtStatusOf(balance),
                balance = format(balance),
                dueDate = dueDate,
                isOverdue = isOverdue(dueDate),
                openSince = entries
                    .filter { balance != 0L && it.kind.isNewDebt && it.kind.balanceSign == openSign }
                    .minOfOrNull { it.date },
                entries = entries.map { entry ->
                    DebtEntryRow(
                        id = entry.id,
                        kind = entry.kind,
                        amount = format(entry.amountMilli),
                        date = entry.date,
                        accountName = entry.accountId?.let { accountNames[it] },
                        note = entry.note,
                        dueDate = entry.dueDate,
                    )
                },
            )
        }.onEach { newState -> newState?.let { value -> _state.update { value } } }
            .launchIn(viewModelScope)
    }

    fun processAction(action: DebtPersonAction) {
        when (action) {
            DebtPersonAction.ClosePage -> appComposeNavigator.popBackStack()

            is DebtPersonAction.AddEntry -> appComposeNavigator.navigate(
                ExpenseManagerScreens.DebtEntry(personId = personId, kind = action.kind.name),
            )

            DebtPersonAction.SettleUp -> appComposeNavigator.navigate(
                ExpenseManagerScreens.DebtEntry(personId = personId, settle = true),
            )

            is DebtPersonAction.OpenEntry ->
                appComposeNavigator.navigate(ExpenseManagerScreens.DebtEntry(id = action.id))

            DebtPersonAction.ReminderShared ->
                analyticsRepository.logEvent(AnalyticsEvents.DEBT_REMINDER_SHARED, emptyMap())

            DebtPersonAction.ShowRename -> _state.update { it.copy(showRenameDialog = true) }

            DebtPersonAction.DismissRename -> _state.update { it.copy(showRenameDialog = false) }

            is DebtPersonAction.Rename -> rename(action.name)

            DebtPersonAction.ShowDelete -> _state.update { it.copy(showDeleteDialog = true) }

            DebtPersonAction.DismissDelete -> _state.update { it.copy(showDeleteDialog = false) }

            DebtPersonAction.ConfirmDelete -> viewModelScope.launch {
                _state.update { it.copy(showDeleteDialog = false) }
                debtRepository.deletePerson(personId)
                analyticsRepository.logEvent(AnalyticsEvents.DEBT_PERSON_DELETED, emptyMap())
                // The person flow emits null next, which closes the page.
            }
        }
    }

    private fun rename(name: String) {
        val trimmed = name.trim()
        _state.update { it.copy(showRenameDialog = false) }
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val person = debtRepository.findPerson(personId) ?: return@launch
            debtRepository.savePerson(person.copy(name = trimmed, updatedOn = Date()))
        }
    }
}
