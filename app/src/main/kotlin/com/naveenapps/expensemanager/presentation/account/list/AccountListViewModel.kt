package com.naveenapps.expensemanager.presentation.account.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.domain.model.Account
import com.naveenapps.expensemanager.domain.model.AccountType
import com.naveenapps.expensemanager.domain.model.Currency
import com.naveenapps.expensemanager.domain.model.UiState
import com.naveenapps.expensemanager.domain.usecase.account.GetAccountsUseCase
import com.naveenapps.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.presentation.transaction.list.getAmountTextColor
import com.naveenapps.expensemanager.ui.utils.UiText
import com.naveenapps.expensemanager.ui.utils.getCurrency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class AccountListViewModel @Inject constructor(
    getAccountsUseCase: GetAccountsUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
) : ViewModel() {

    private val _errorMessage = MutableSharedFlow<UiText>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _accounts = MutableStateFlow<UiState<List<AccountUiModel>>>(UiState.Loading)
    val accounts = _accounts.asStateFlow()

    init {

        getCurrencyUseCase.invoke().combine(getAccountsUseCase.invoke()) { currency, accounts ->
            currency to accounts
        }.map { currencyAndAccountPair ->

            val (currency, accounts) = currencyAndAccountPair

            _accounts.value = if (accounts.isEmpty()) {
                UiState.Empty
            } else {
                UiState.Success(
                    accounts.map {
                        it.toAccountUiModel(currency)
                    }
                )
            }
        }.launchIn(viewModelScope)
    }
}

fun Account.toAccountUiModel(currency: Currency) = AccountUiModel(
    id = this.id,
    name = this.name,
    icon = this.iconName,
    iconBackgroundColor = this.iconBackgroundColor,
    amount = getCurrency(
        currency,
        this.amount
    ),
    type = this.type,
    amountTextColor = this.amount.getAmountTextColor()
)


data class AccountUiModel(
    val id: String,
    val name: String,
    val icon: String,
    val iconBackgroundColor: String,
    val amount: UiText,
    val amountTextColor: Int,
    val type: AccountType = AccountType.REGULAR
)