package com.naveenapps.expensemanager.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetDefaultCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding.GetOnboardingStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding.SetOnboardingStatusUseCase
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
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
    getAllAccountsUseCase: GetAllAccountsUseCase,
    getDefaultCurrencyUseCase: GetDefaultCurrencyUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    private val setOnboardingStatusUseCase: SetOnboardingStatusUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val composeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<AccountUiModel>>(emptyList())
    val accounts = _accounts.asStateFlow()

    private val _currency = MutableStateFlow(getDefaultCurrencyUseCase())
    val currency = _currency.asStateFlow()

    init {
        viewModelScope.launch {
            if (getOnboardingStatusUseCase.invoke()) {
                openHome()
            }
        }

        combine(
            getCurrencyUseCase.invoke(),
            getAllAccountsUseCase.invoke()
        ) { currency, accounts ->
            _currency.value = currency
            _accounts.value = accounts.map { account ->
                account.toAccountUiModel(
                    getFormattedAmountUseCase.invoke(account.amount, currency)
                )
            }
        }.launchIn(viewModelScope)
    }

    fun openHome() {
        viewModelScope.launch {
            setOnboardingStatusUseCase.invoke(true)
            composeNavigator.navigateAndClearBackStack(ExpenseManagerScreens.Home.route)
        }
    }

    fun openAccountCreateScreen(accountId: String?) {
        composeNavigator.navigate(
            ExpenseManagerScreens.AccountCreate.createRoute(
                accountId ?: ""
            )
        )
    }
}