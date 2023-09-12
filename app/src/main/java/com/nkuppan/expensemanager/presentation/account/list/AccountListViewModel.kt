package com.nkuppan.expensemanager.presentation.account.list

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.core.ui.utils.getPaymentModeIcon
import com.nkuppan.expensemanager.core.utils.AppCoroutineDispatchers
import com.nkuppan.expensemanager.domain.model.Account
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.usecase.account.GetAccountByIdUseCase
import com.nkuppan.expensemanager.domain.usecase.account.GetAccountsByNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountListViewModel @Inject constructor(
    private val getAccountByIdUseCase: GetAccountByIdUseCase,
    private val getAccountsByNameUseCase: GetAccountsByNameUseCase,
    private val appCoroutineDispatchers: AppCoroutineDispatchers
) : ViewModel() {

    private val _errorMessage = Channel<UiText>()
    val errorMessage = _errorMessage.receiveAsFlow()

    private val _accounts = Channel<List<AccountUIState>>()
    val accounts = _accounts.receiveAsFlow()

    private val _openAccount = Channel<Account>()
    val openAccount = _openAccount.receiveAsFlow()

    fun loadAccounts(searchValue: String? = "") {

        viewModelScope.launch(appCoroutineDispatchers.io) {

            when (val response = getAccountsByNameUseCase.invoke(searchValue)) {
                is Resource.Error -> {
                    _accounts.send(emptyList())
                }

                is Resource.Success -> {
                    _accounts.send(
                        response.data.map {
                            AccountUIState(
                                it.id,
                                it.name,
                                it.backgroundColor,
                                it.type.getPaymentModeIcon(),
                            )
                        }
                    )
                }
            }
        }
    }

    fun openAccountToEdit(accountId: String) {
        viewModelScope.launch {
            when (val response = getAccountByIdUseCase.invoke(accountId)) {
                is Resource.Error -> {
                    _errorMessage.send(UiText.StringResource(R.string.account_delete_error_message))
                }

                is Resource.Success -> {
                    _openAccount.send(response.data)
                }
            }
        }
    }
}

data class AccountUIState(
    val id: String,
    val name: String,
    val backgroundColor: String,
    @DrawableRes val accountIcon: Int
)