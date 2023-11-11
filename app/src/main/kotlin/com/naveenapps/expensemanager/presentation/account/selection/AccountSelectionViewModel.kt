package com.naveenapps.expensemanager.presentation.account.selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.presentation.account.list.toAccountUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountSelectionViewModel @Inject constructor(
    getCurrencyUseCase: GetCurrencyUseCase,
    getFormattedAmountUseCase: GetFormattedAmountUseCase,
    getAccountsUseCase: GetAccountsUseCase,
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<AccountUiModel>>(emptyList())
    val accounts = _accounts.asStateFlow()

    private val _selectedAccounts = MutableStateFlow<List<AccountUiModel>>(emptyList())
    val selectedAccounts = _selectedAccounts.asStateFlow()

    init {

        combine(
            getCurrencyUseCase.invoke(),
            getAccountsUseCase.invoke()
        ) { currency, accounts ->

            _accounts.value = accounts.map {
                it.toAccountUiModel(
                    getFormattedAmountUseCase.invoke(
                        it.amount,
                        currency
                    )
                )
            }
            _selectedAccounts.value = accounts.map {
                it.toAccountUiModel(
                    getFormattedAmountUseCase.invoke(
                        it.amount,
                        currency
                    )
                )
            }
        }.launchIn(viewModelScope)
    }

    fun clearChanges() {
        _selectedAccounts.value = emptyList()
    }

    fun selectThisAccount(account: AccountUiModel, selected: Boolean) {
        viewModelScope.launch {
            val selectedAccounts = _selectedAccounts.value.toMutableList()

            val selectedAccount = selectedAccounts.firstOrNull {
                account.id == it.id
            }

            if (selectedAccount != null) {
                if (selected.not()) {
                    selectedAccounts.remove(selectedAccount)
                }
            } else {
                if (selected) {
                    selectedAccounts.add(account)
                }
            }

            _selectedAccounts.value = selectedAccounts
        }
    }
}