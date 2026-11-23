package com.naveenapps.expensemanager.feature.debt.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.debt.DebtLedger
import com.naveenapps.expensemanager.core.domain.usecase.debt.GetDebtBalancesUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.DebtEntryKind
import com.naveenapps.expensemanager.core.model.milliToDouble
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.feature.debt.DebtStatus
import com.naveenapps.expensemanager.feature.debt.debtStatusOf
import com.naveenapps.expensemanager.feature.debt.isOverdue
import kotlin.math.abs
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class DebtPersonRow(
    val id: String,
    val name: String,
    val status: DebtStatus,
    /** The open balance, formatted without a sign (the status says which way). */
    val amount: String,
    val dueDate: Instant?,
    val isOverdue: Boolean,
)

data class DebtListState(
    val isLoaded: Boolean = false,
    val toGet: String = "",
    val toGive: String = "",
    val people: List<DebtPersonRow> = emptyList(),
)

sealed class DebtListAction {
    data object ClosePage : DebtListAction()
    data class OpenPerson(val id: String) : DebtListAction()
    data class AddEntry(val kind: DebtEntryKind) : DebtListAction()
}

/** People & debts: everyone with their open balance, plus what the user gets / gives in total. */
class DebtListViewModel(
    getDebtBalancesUseCase: GetDebtBalancesUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _state = MutableStateFlow(DebtListState())
    val state = _state.asStateFlow()

    init {
        combine(getDebtBalancesUseCase(), getCurrencyUseCase()) { balances, currency ->
            fun format(milli: Long): String = getFormattedAmountUseCase(abs(milli).milliToDouble(), currency).amountString.orEmpty()
            val totals = DebtLedger.totals(balances)
            DebtListState(
                isLoaded = true,
                toGet = format(totals.toGetMilli),
                toGive = format(totals.toGiveMilli),
                people = balances.map { balance ->
                    DebtPersonRow(
                        id = balance.person.id,
                        name = balance.person.name,
                        status = debtStatusOf(balance.balanceMilli),
                        amount = format(balance.balanceMilli),
                        dueDate = balance.dueDate,
                        isOverdue = isOverdue(balance.dueDate),
                    )
                },
            )
        }.onEach { newState -> _state.update { newState } }
            .launchIn(viewModelScope)
    }

    fun processAction(action: DebtListAction) {
        when (action) {
            DebtListAction.ClosePage -> appComposeNavigator.popBackStack()

            is DebtListAction.OpenPerson -> appComposeNavigator.navigate(ExpenseManagerScreens.DebtPerson(action.id))

            is DebtListAction.AddEntry -> appComposeNavigator.navigate(
                ExpenseManagerScreens.DebtEntry(kind = action.kind.name),
            )
        }
    }
}
