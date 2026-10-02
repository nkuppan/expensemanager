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
import com.naveenapps.expensemanager.core.domain.usecase.settings.reminder.GetReminderStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.reminder.GetReminderTimeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.reminder.UpdateReminderStatusUseCase
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.ReminderTimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    private val getReminderStatusUseCase: GetReminderStatusUseCase,
    private val getReminderTimeUseCase: GetReminderTimeUseCase,
    private val updateReminderStatusUseCase: UpdateReminderStatusUseCase,
    private val reminderTimeRepository: ReminderTimeRepository,
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
                loadReminder()
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
                viewModelScope.launch {
                    saveReminderChoice()
                    openHome()
                }
            }

            OnboardingAction.AddFirstExpense -> {
                logOnboardingCompleted(METHOD_ADD_EXPENSE)
                viewModelScope.launch {
                    saveReminderChoice()
                    openFirstTransaction()
                }
            }

            is OnboardingAction.SetReminderEnabled -> {
                _state.update { it.copy(reminderEnabled = action.enabled) }
            }

            is OnboardingAction.NotificationPermissionResult -> {
                analyticsRepository.logEvent(
                    AnalyticsEvents.NOTIFICATION_PERMISSION_RESULT,
                    mapOf(
                        AnalyticsParams.GRANTED to action.granted.toString(),
                        AnalyticsParams.METHOD to METHOD_ONBOARDING,
                    ),
                )
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
                    val currency = action.country.currency
                    saveCurrencyUseCase.invoke(currency)
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
                AnalyticsParams.REMINDER_ENABLED to current.reminderEnabled.toString(),
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

    private suspend fun loadReminder() {
        val enabled = getReminderStatusUseCase.invoke().first()
        val time = getReminderTimeUseCase.invoke().first()
        _state.update { it.copy(reminderEnabled = enabled, reminderTime = time) }
    }

    /**
     * Persists the switch and records that the notification question has been asked here,
     * so Home's post-first-expense explainer doesn't ask a second time. Home reschedules the
     * reminder on arrival once the permission is in place.
     */
    private suspend fun saveReminderChoice() {
        updateReminderStatusUseCase.invoke(_state.value.reminderEnabled)
        reminderTimeRepository.setNotificationPrimerShown()
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
        const val METHOD_ONBOARDING = "onboarding"
    }
}
