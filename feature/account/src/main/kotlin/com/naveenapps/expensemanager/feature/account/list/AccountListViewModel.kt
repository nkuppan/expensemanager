package com.naveenapps.expensemanager.feature.account.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.getAvailableCreditLimit
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AccountListViewModel @Inject constructor(
    getAllAccountsUseCase: GetAllAccountsUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _state = MutableStateFlow(
        AccountListState(
            accounts = emptyList(),
            showReOrder = false
        )
    )
    val state = _state.asStateFlow()

    init {
        combine(
            getCurrencyUseCase.invoke(),
            getAllAccountsUseCase.invoke(),
        ) { currency, accounts ->
            val list = accounts.map {
                it.toAccountUiModel(
                    getFormattedAmountUseCase.invoke(
                        it.amount,
                        currency,
                    ),
                    if (it.type == AccountType.CREDIT) {
                        getFormattedAmountUseCase.invoke(
                            it.getAvailableCreditLimit(),
                            currency
                        )
                    } else {
                        null
                    }
                )
            }

            _state.update {
                it.copy(
                    accounts = list,
                    showReOrder = accounts.isNotEmpty() && accounts.size > 1
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun openCreateScreen(account: AccountUiModel?) {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.AccountCreate(account?.id),
        )
    }

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    private fun openAccountReOrderScreen() {
        appComposeNavigator.navigate(ExpenseManagerScreens.AccountReOrderScreen)
    }

    fun processAction(action: AccountListAction) {
        when (action) {
            AccountListAction.ClosePage -> closePage()
            AccountListAction.CreateAccount -> openCreateScreen(null)
            is AccountListAction.EditAccount -> openCreateScreen(action.accountUiModel)
            AccountListAction.OpenReOrder -> openAccountReOrderScreen()
        }
    }
}
