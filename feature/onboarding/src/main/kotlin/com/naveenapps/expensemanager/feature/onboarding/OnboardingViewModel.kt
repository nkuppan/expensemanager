package com.naveenapps.expensemanager.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.GetOnboardingStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.feature.account.list.toAccountUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
    getAccountsUseCase: GetAccountsUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<AccountUiModel>>(emptyList())
    val accounts = _accounts.asStateFlow()

    private val _currency = MutableStateFlow(
        Currency(
            com.naveenapps.expensemanager.core.common.R.string.dollar_type,
            com.naveenapps.expensemanager.core.common.R.string.dollar_name,
            com.naveenapps.expensemanager.core.common.R.drawable.currency_dollar
        )
    )
    val currency = _currency.asStateFlow()

    init {
        combine(
            getCurrencyUseCase.invoke(),
            getAccountsUseCase.invoke()
        ) { currency, accounts ->
            _currency.value = currency
            _accounts.value = accounts.map { account ->
                account.toAccountUiModel(
                    getFormattedAmountUseCase.invoke(account.amount, currency)
                )
            }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            val status = getOnboardingStatusUseCase.invoke()
            if (status) {

            }
        }
    }
}