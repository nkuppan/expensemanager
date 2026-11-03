package com.naveenapps.expensemanager.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.AddAccountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.account.DeleteAccountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAccountUiModelUseCase
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetDefaultCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.SaveCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding.GetOnboardingStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding.SetOnboardingStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.reminder.GetReminderStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.reminder.GetReminderTimeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.reminder.UpdateReminderStatusUseCase
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.ReminderTimeRepository
import java.util.UUID
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
    private val addAccountUseCase: AddAccountUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val setOnboardingStatusUseCase: SetOnboardingStatusUseCase,
    private val getAccountUiModelUseCase: GetAccountUiModelUseCase,
    private val composeNavigator: AppComposeNavigator,
    private val analyticsRepository: AnalyticsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        OnboardingState(
            currency = getDefaultCurrencyUseCase.invoke(),
            accounts = emptyList(),
            showCurrencySelection = false,
        ),
    )
    val state = _state.asStateFlow()

    private var accounts: List<Account> = emptyList()

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
                    this@OnboardingViewModel.accounts = accounts
                    _state.update {
                        it.copy(
                            currency = currency,
                            accounts = accounts.map { account -> getAccountUiModelUseCase(account) },
                            showCurrencySelection = false,
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
            OnboardingAction.Next -> {
                logOnboardingCompleted(METHOD_EXPLORE)
                openHome()
            }

            OnboardingAction.AddFirstExpense -> {
                logOnboardingCompleted(METHOD_ADD_EXPENSE)
                viewModelScope.launch { openFirstTransaction() }
            }

            OnboardingAction.OpenAccounts -> {
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
                    val previousCode = _state.value.currency.code
                    val currency = action.country.currency
                    if (saveCurrencyUseCase.invoke(currency) !is Resource.Success) return@launch
                    // Accounts still in the previous main currency were only defaulted to it (the
                    // preloaded ones, or ones created here without picking another currency).
                    // An account's currency can never change, so they are recreated in the new
                    // main currency instead; this is safe only because onboarding precedes any
                    // transaction (delete refuses accounts that have one).
                    accounts
                        .filter { it.currencyCode == previousCode }
                        .forEach { account ->
                            // Add the re-stamped copy before deleting the original: the last
                            // remaining account can never be deleted. These starter accounts
                            // have no transactions yet, so the new id is safe.
                            val restamped = account.copy(
                                id = UUID.randomUUID().toString(),
                                currencyCode = currency.code,
                            )
                            if (addAccountUseCase(restamped) is Resource.Success) {
                                deleteAccountUseCase(account)
                            }
                        }
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
        const val METHOD_EXPLORE = "explore"
        const val METHOD_ADD_EXPENSE = "add_expense"
    }
}
