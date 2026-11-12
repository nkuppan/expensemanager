package com.naveenapps.expensemanager.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetDefaultCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.PreselectCountryCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.SaveCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding.GetOnboardingStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding.SetOnboardingStatusUseCase
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
    getAllAccountsUseCase: GetAllAccountsUseCase,
    getDefaultCurrencyUseCase: GetDefaultCurrencyUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    private val saveCurrencyUseCase: SaveCurrencyUseCase,
    private val setOnboardingStatusUseCase: SetOnboardingStatusUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val composeNavigator: AppComposeNavigator,
    private val analyticsRepository: AnalyticsRepository,
    private val preselectCountryCurrencyUseCase: PreselectCountryCurrencyUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(
        OnboardingState(
            currency = getDefaultCurrencyUseCase.invoke(),
            accounts = emptyList(),
            showCurrencySelection = false,
        ),
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (getOnboardingStatusUseCase.invoke()) {
                openHome()
            } else {
                // Setup is the first screen a new user sees (Intro was merged into it), so this is
                // the activation funnel's entry step. init runs once per screen instance, not on
                // recomposition or rotation.
                analyticsRepository.logEvent(AnalyticsEvents.INTRO_GET_STARTED, emptyMap())
                combine(
                    getCurrencyUseCase.invoke(),
                    getAllAccountsUseCase.invoke(),
                ) { currency, accounts ->
                    _state.update { it ->
                        it.copy(
                            currency = currency,
                            accounts = accounts.map {
                                it.toAccountUiModel(
                                    getFormattedAmountUseCase.invoke(it.amount, currency),
                                )
                            },
                            showCurrencySelection = false,
                        )
                    }
                }.launchIn(viewModelScope)
                // After the screen is wired up, so reading the country list never delays it.
                preselectCurrency()
            }
        }
    }

    // A fresh install starts on the user's own currency (from the SIM, network or region),
    // shown straight away through the currency flow below. Never overrides a saved choice.
    private suspend fun preselectCurrency() {
        val detected = runCatching { preselectCountryCurrencyUseCase() }.getOrNull() ?: return
        analyticsRepository.logEvent(
            AnalyticsEvents.ONBOARDING_CURRENCY_DETECTED,
            mapOf(
                AnalyticsParams.COUNTRY to detected.country.countryCode,
                AnalyticsParams.CURRENCY to detected.country.currency.code,
                AnalyticsParams.SOURCE to detected.source,
            ),
        )
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
            OnboardingAction.GetStarted -> {
                logOnboardingCompleted(METHOD_GET_STARTED)
                viewModelScope.launch { openFirstTransaction() }
            }

            OnboardingAction.OpenAccounts -> {
                analyticsRepository.logEvent(AnalyticsEvents.ONBOARDING_ACCOUNTS_OPENED, emptyMap())
                composeNavigator.navigate(ExpenseManagerScreens.AccountList)
            }

            is OnboardingAction.AccountCreate -> openAccountCreateScreen(action.account?.id)

            OnboardingAction.DismissCurrencySelection -> {
                dismissCurrencySelection()
            }

            OnboardingAction.ShowCurrencySelection -> {
                showCurrencySelection()
            }

            is OnboardingAction.SelectCurrency -> {
                viewModelScope.launch {
                    val currency = action.country.currency
                    saveCurrencyUseCase.invoke(currency)
                    // Tells whether people actually change the pre-selected currency.
                    analyticsRepository.logEvent(
                        AnalyticsEvents.ONBOARDING_CURRENCY_CHANGED,
                        mapOf(
                            AnalyticsParams.CURRENCY to currency.code,
                            AnalyticsParams.COUNTRY to action.country.countryCode,
                        ),
                    )
                }
            }
        }
    }

    // Logged only from the user's own tap, not from the init path that skips onboarding
    // for returning users, so the count is new completions only.
    private fun logOnboardingCompleted(method: String) {
        val current = _state.value
        analyticsRepository.logEvent(
            AnalyticsEvents.ONBOARDING_COMPLETED,
            mapOf(
                AnalyticsParams.METHOD to method,
                AnalyticsParams.CURRENCY to current.currency.code,
                AnalyticsParams.ACCOUNT_COUNT to current.accounts.size.toString(),
            ),
        )
    }

    // Home becomes the root with the new-transaction screen on top, so saving (or backing out)
    // lands the user on Home, never back on setup.
    private suspend fun openFirstTransaction() {
        setOnboardingStatusUseCase.invoke(true)
        composeNavigator.resetBackStackTo(
            listOf(
                ExpenseManagerScreens.Home,
                ExpenseManagerScreens.TransactionCreate(null),
            ),
        )
    }

    private fun showCurrencySelection() {
        _state.update { it.copy(showCurrencySelection = true) }
    }

    private fun dismissCurrencySelection() {
        _state.update { it.copy(showCurrencySelection = false) }
    }

    private companion object {
        // Was "explore" or "add_expense" up to 1.4.29, when setup had two buttons.
        const val METHOD_GET_STARTED = "get_started"
    }
}
