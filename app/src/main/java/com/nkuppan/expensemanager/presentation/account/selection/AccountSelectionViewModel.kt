package com.nkuppan.expensemanager.presentation.account.selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.domain.usecase.account.GetAccountsUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.presentation.account.list.AccountUiModel
import com.nkuppan.expensemanager.presentation.account.list.toAccountUiModel
import com.nkuppan.expensemanager.ui.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountSelectionViewModel @Inject constructor(
    getAccountsUseCase: GetAccountsUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
) : ViewModel() {

    private val _errorMessage = MutableSharedFlow<UiText>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _accounts = MutableStateFlow<List<AccountUiModel>>(emptyList())
    val accounts = _accounts.asStateFlow()

    private val _selectedAccounts = MutableStateFlow<List<AccountUiModel>>(emptyList())
    val selectedAccounts = _selectedAccounts.asStateFlow()

    init {

        getCurrencyUseCase.invoke().combine(getAccountsUseCase.invoke()) { currency, accounts ->
            currency to accounts
        }.map { currencyAndAccountPair ->

            val (currency, accounts) = currencyAndAccountPair

            _accounts.value = accounts.map { it.toAccountUiModel(currency) }
            _selectedAccounts.value = accounts.map { it.toAccountUiModel(currency) }
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