package com.naveenapps.expensemanager.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetDefaultCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.locale.GetCurrentLocaleUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.theme.GetCurrentThemeUseCase
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class SettingsViewModel(
    getDefaultCurrencyUseCase: GetDefaultCurrencyUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    getCurrentThemeUseCase: GetCurrentThemeUseCase,
    getCurrentLocaleUseCase: GetCurrentLocaleUseCase,
    getAllAccountsUseCase: GetAllAccountsUseCase,
    getAllCategoryUseCase: GetAllCategoryUseCase,
    private val settingsRepository: SettingsRepository,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _event = Channel<SettingEvent>()
    val event = _event.receiveAsFlow()

    private val _state = MutableStateFlow(
        SettingState(
            currency = getDefaultCurrencyUseCase.invoke(),
            theme = null,
            showThemeSelection = false
        )
    )
    val state = _state.asStateFlow()

    init {
        getCurrencyUseCase.invoke().onEach { currency ->
            _state.update { it.copy(currency = currency) }
        }.launchIn(viewModelScope)

        getCurrentThemeUseCase.invoke().onEach { theme ->
            _state.update { it.copy(theme = theme) }
        }.launchIn(viewModelScope)

        getCurrentLocaleUseCase.invoke().onEach { locale ->
            _state.update { it.copy(locale = locale) }
        }.launchIn(viewModelScope)

        // Defaults + Security section (moved here from AdvancedSettingsViewModel)
        settingsRepository.getHomeSummaryCompact().onEach { compact ->
            _state.update { it.copy(isCompactSummary = compact) }
        }.launchIn(viewModelScope)

        settingsRepository.isAppLockEnabled().onEach { enabled ->
            _state.update { it.copy(isAppLockEnabled = enabled) }
        }.launchIn(viewModelScope)

        getAllAccountsUseCase.invoke().onEach { accounts ->
            val accountId = settingsRepository.getDefaultAccount().firstOrNull()
            val account = accounts.find { it.id == accountId }
            _state.update {
                it.copy(
                    accounts = accounts,
                    selectedAccount = account ?: accounts.firstOrNull()
                )
            }
        }.launchIn(viewModelScope)

        getAllCategoryUseCase.invoke().onEach { categories ->
            val (expenses, incomes) = categories.partition { category -> category.type.isExpense() }

            val expenseCategoryId = settingsRepository.getDefaultExpenseCategory().firstOrNull()
            val expenseCategory = expenses.find { it.id == expenseCategoryId }

            val incomeCategoryId = settingsRepository.getDefaultIncomeCategory().firstOrNull()
            val incomeCategory = incomes.find { it.id == incomeCategoryId }

            _state.update {
                it.copy(
                    expenseCategories = expenses,
                    selectedExpenseCategory = expenseCategory ?: expenses.firstOrNull(),
                    incomeCategories = incomes,
                    selectedIncomeCategory = incomeCategory ?: incomes.firstOrNull(),
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun openCurrencyCustomiseScreen() {
        appComposeNavigator.navigate(ExpenseManagerScreens.CurrencyCustomiseScreen)
    }

    private fun openExportScreen() {
        appComposeNavigator.navigate(ExpenseManagerScreens.ExportScreen)
    }

    private fun openNotificationScreen() {
        appComposeNavigator.navigate(ExpenseManagerScreens.ReminderScreen)
    }

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    private fun openAboutUs() {
        appComposeNavigator.navigate(ExpenseManagerScreens.AboutUsScreen)
    }

    private fun openAdvancedSettings() {
        appComposeNavigator.navigate(ExpenseManagerScreens.AdvancedSettingsScreen)
    }

    private fun changeDefaultAccount(account: Account) {
        viewModelScope.launch {
            when (val response = settingsRepository.setDefaultAccount(account.id)) {
                is Resource.Error -> Unit
                is Resource.Success -> {
                    if (response.data) {
                        _state.update { it.copy(selectedAccount = account) }
                    }
                }
            }
        }
    }

    private fun changeSelectedExpenseCategory(category: Category) {
        viewModelScope.launch {
            when (val response = settingsRepository.setDefaultExpenseCategory(category.id)) {
                is Resource.Error -> Unit
                is Resource.Success -> {
                    if (response.data) {
                        _state.update { it.copy(selectedExpenseCategory = category) }
                    }
                }
            }
        }
    }

    private fun changeSelectedIncomeCategory(category: Category) {
        viewModelScope.launch {
            when (val response = settingsRepository.setDefaultIncomeCategory(category.id)) {
                is Resource.Error -> Unit
                is Resource.Success -> {
                    if (response.data) {
                        _state.update { it.copy(selectedIncomeCategory = category) }
                    }
                }
            }
        }
    }

    fun processAction(action: SettingAction) {
        when (action) {
            SettingAction.ClosePage -> closePage()
            SettingAction.OpenAboutUs -> openAboutUs()
            SettingAction.OpenAdvancedSettings -> openAdvancedSettings()
            SettingAction.OpenCurrencyEdit -> openCurrencyCustomiseScreen()
            SettingAction.OpenExport -> openExportScreen()
            SettingAction.OpenNotification -> openNotificationScreen()
            SettingAction.OpenRateUs -> {
                viewModelScope.launch {
                    _event.send(SettingEvent.RateUs)
                }
            }

            SettingAction.DismissThemeSelection -> {
                _state.update { it.copy(showThemeSelection = false) }
            }

            SettingAction.ShowThemeSelection -> {
                _state.update { it.copy(showThemeSelection = true) }
            }

            SettingAction.DismissLanguageSelection -> {
                _state.update { it.copy(showLanguageSelection = false) }
            }

            SettingAction.ShowLanguageSelection -> {
                _state.update { it.copy(showLanguageSelection = true) }
            }

            is SettingAction.SelectAccount -> changeDefaultAccount(action.account)

            is SettingAction.SelectExpenseCategory -> changeSelectedExpenseCategory(action.category)

            is SettingAction.SelectIncomeCategory -> changeSelectedIncomeCategory(action.category)

            SettingAction.ToggleCompactSummary -> {
                viewModelScope.launch {
                    val newValue = !_state.value.isCompactSummary
                    settingsRepository.setHomeSummaryCompact(newValue)
                    _state.update { it.copy(isCompactSummary = newValue) }
                }
            }

            SettingAction.ToggleAppLock -> {
                viewModelScope.launch {
                    val newValue = !_state.value.isAppLockEnabled
                    settingsRepository.setAppLockEnabled(newValue)
                    _state.update { it.copy(isAppLockEnabled = newValue) }
                }
            }

            SettingAction.Backup -> {
                viewModelScope.launch {
                    _event.send(SettingEvent.Backup)
                }
            }

            SettingAction.Restore -> {
                viewModelScope.launch {
                    _event.send(SettingEvent.Restore)
                }
            }
        }
    }
}
