package com.nkuppan.expensemanager.feature.account.list

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.nkuppan.expensemanager.core.model.Account
import com.nkuppan.expensemanager.core.model.PaymentMode
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.data.usecase.account.GetAccountByIdUseCase
import com.nkuppan.expensemanager.data.usecase.account.GetAccountsByNameUseCase
import com.nkuppan.expensemanager.feature.account.R
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
                                getPaymentModeIcon(it.type),
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


@DrawableRes
fun getPaymentModeIcon(paymentMode: PaymentMode): Int {
    return when (paymentMode) {
        PaymentMode.CARD -> R.drawable.ic_card
        PaymentMode.WALLET -> R.drawable.ic_wallet
        PaymentMode.UPI -> R.drawable.ic_upi
        PaymentMode.CHEQUE -> R.drawable.ic_cheque
        PaymentMode.INTERNET_BANKING -> R.drawable.ic_netbanking
        PaymentMode.BANK_ACCOUNT -> R.drawable.ic_bank
        PaymentMode.NONE -> R.drawable.ic_wallet
    }
}

data class AccountUIState(
    val id: String,
    val name: String,
    val backgroundColor: String,
    @DrawableRes val accountIcon: Int
)