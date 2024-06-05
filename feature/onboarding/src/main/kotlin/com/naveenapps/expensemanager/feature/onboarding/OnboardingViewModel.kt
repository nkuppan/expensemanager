package com.naveenapps.expensemanager.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetDefaultCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.SaveCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding.GetOnboardingStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding.SetOnboardingStatusUseCase
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
    getAllAccountsUseCase: GetAllAccountsUseCase,
    getDefaultCurrencyUseCase: GetDefaultCurrencyUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    private val saveCurrencyUseCase: SaveCurrencyUseCase,
    private val setOnboardingStatusUseCase: SetOnboardingStatusUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val composeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _state = MutableStateFlow(
        OnboardingState(
            currency = getDefaultCurrencyUseCase.invoke(),
            accounts = emptyList(),
            showCurrencySelection = false
        )
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (getOnboardingStatusUseCase.invoke()) {
                openHome()
            } else {
                combine(
                    getCurrencyUseCase.invoke(),
                    getAllAccountsUseCase.invoke(),
                ) { currency, accounts ->
                    _state.update {
                        it.copy(
                            currency = currency,
                            accounts = accounts.map {
                                it.toAccountUiModel(
                                    getFormattedAmountUseCase.invoke(it.amount, currency),
                                )
                            },
                            showCurrencySelection = false
                        )
                    }
                }.launchIn(viewModelScope)
            }
        }
    }

    private fun openHome() {
        viewModelScope.launch {
            setOnboardingStatusUseCase.invoke(true)
            composeNavigator.navigateAndClearBackStack(ExpenseManagerScreens.Home)
        }
    }

    private fun openAccountCreateScreen(accountId: String?) {
        composeNavigator.navigate(ExpenseManagerScreens.AccountCreate(accountId))
    }

    fun processAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.Next -> openHome()
            is OnboardingAction.AccountCreate -> openAccountCreateScreen(action.account?.id)
            OnboardingAction.DismissCurrencySelection -> {
                dismissCurrencySelection()
            }

            OnboardingAction.ShowCurrencySelection -> {
                showCurrencySelection()
            }

            is OnboardingAction.SelectCurrency -> {
                viewModelScope.launch {
                    if (action.currency != null) {
                        saveCurrencyUseCase.invoke(action.currency)
                    } else {
                        dismissCurrencySelection()
                    }
                }
            }
        }
    }

    private fun showCurrencySelection() {
        _state.update { it.copy(showCurrencySelection = true) }
    }

    private fun dismissCurrencySelection() {
        _state.update { it.copy(showCurrencySelection = false) }
    }
}
