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
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.BackupException
import com.naveenapps.expensemanager.core.repository.BackupRepository
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
    private val analyticsRepository: AnalyticsRepository,
    private val backupRepository: BackupRepository,
) : ViewModel() {

    private val _event = Channel<SettingEvent>()
    val event = _event.receiveAsFlow()

    private val _state = MutableStateFlow(
        SettingState(
            currency = getDefaultCurrencyUseCase.invoke(),
            theme = null,
            showThemeSelection = false,
        ),
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
                    selectedAccount = account ?: accounts.firstOrNull(),
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

            SettingAction.OpenRecurringTransactions ->
                appComposeNavigator.navigate(ExpenseManagerScreens.RecurringTransactions)

            SettingAction.OpenNotification -> openNotificationScreen()

            SettingAction.ShareApp -> {
                viewModelScope.launch {
                    analyticsRepository.logEvent(AnalyticsEvents.SHARE_APP, emptyMap())
                    _event.send(SettingEvent.ShareApp)
                }
            }

            SettingAction.OpenRateUs -> {
                viewModelScope.launch {
                    analyticsRepository.logEvent(AnalyticsEvents.RATE_US_CLICKED, emptyMap())
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
                if (_state.value.isBackupInProgress) return
                viewModelScope.launch {
                    analyticsRepository.logEvent(AnalyticsEvents.BACKUP_STARTED, emptyMap())
                    _event.send(SettingEvent.PickBackupDestination(backupRepository.backupFileName()))
                }
            }

            is SettingAction.BackupDestinationSelected -> {
                val uri = action.uri ?: return
                runBackupTask(
                    task = { backupRepository.backupData(uri) },
                    onSuccess = {
                        analyticsRepository.logEvent(AnalyticsEvents.BACKUP_COMPLETED, emptyMap())
                        _event.send(SettingEvent.ShowMessage(R.string.backup_success))
                    },
                    failureMessage = { R.string.backup_failed },
                )
            }

            SettingAction.Restore -> {
                if (_state.value.isBackupInProgress) return
                _state.update { it.copy(showRestoreConfirmation = true) }
            }

            SettingAction.DismissRestoreConfirmation -> {
                _state.update { it.copy(showRestoreConfirmation = false) }
            }

            SettingAction.ConfirmRestore -> {
                _state.update { it.copy(showRestoreConfirmation = false) }
                viewModelScope.launch {
                    analyticsRepository.logEvent(AnalyticsEvents.RESTORE_STARTED, emptyMap())
                    _event.send(SettingEvent.PickRestoreSource)
                }
            }

            is SettingAction.RestoreSourceSelected -> {
                val uri = action.uri ?: return
                runBackupTask(
                    task = { backupRepository.restoreData(uri) },
                    onSuccess = {
                        analyticsRepository.logEvent(AnalyticsEvents.RESTORE_COMPLETED, emptyMap())
                        _event.send(SettingEvent.RestartApp)
                    },
                    failureMessage = { reason ->
                        when (reason) {
                            BackupException.Reason.INVALID_FILE -> R.string.restore_invalid_file
                            BackupException.Reason.NEWER_VERSION -> R.string.restore_newer_version
                            else -> R.string.restore_failed
                        }
                    },
                )
            }
        }
    }

    private fun runBackupTask(
        task: suspend () -> Resource<Boolean>,
        onSuccess: suspend () -> Unit,
        failureMessage: (BackupException.Reason?) -> Int,
    ) {
        if (_state.value.isBackupInProgress) return
        _state.update { it.copy(isBackupInProgress = true) }
        viewModelScope.launch {
            try {
                when (val result = task()) {
                    is Resource.Success -> onSuccess()

                    is Resource.Error -> {
                        val reason = (result.exception as? BackupException)?.reason
                        _event.send(SettingEvent.ShowMessage(failureMessage(reason)))
                    }
                }
            } finally {
                _state.update { it.copy(isBackupInProgress = false) }
            }
        }
    }
}
