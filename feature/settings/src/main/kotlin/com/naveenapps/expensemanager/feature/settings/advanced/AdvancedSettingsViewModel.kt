package com.naveenapps.expensemanager.feature.settings.advanced

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Settings → Preferences (the screen formerly called Advanced). */
class AdvancedSettingsViewModel(
    getAllAccountsUseCase: GetAllAccountsUseCase,
    getAllCategoryUseCase: GetAllCategoryUseCase,
    private val appComposeNavigator: AppComposeNavigator,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdvancedSettingState())
    val state = _state.asStateFlow()

    init {
        settingsRepository.isAutoOpenCalculator().onEach { enabled ->
            _state.update { it.copy(isAutoOpenCalculator = enabled) }
        }.launchIn(viewModelScope)

        settingsRepository.getLandingPage().onEach { page ->
            _state.update { it.copy(landingPage = page) }
        }.launchIn(viewModelScope)

        settingsRepository.getHomeSummaryCompact().onEach { compact ->
            _state.update { it.copy(isCompactSummary = compact) }
        }.launchIn(viewModelScope)

        getAllAccountsUseCase.invoke().onEach { accounts ->
            val accountId = settingsRepository.getDefaultAccount().firstOrNull()
            _state.update {
                it.copy(
                    accounts = accounts,
                    selectedAccount = accounts.find { account -> account.id == accountId } ?: accounts.firstOrNull(),
                )
            }
        }.launchIn(viewModelScope)

        getAllCategoryUseCase.invoke().onEach { categories ->
            val (expenses, incomes) = categories.partition { category -> category.type.isExpense() }
            val expenseCategoryId = settingsRepository.getDefaultExpenseCategory().firstOrNull()
            val incomeCategoryId = settingsRepository.getDefaultIncomeCategory().firstOrNull()
            val expenseCategory = expenses.find { it.id == expenseCategoryId } ?: expenses.firstOrNull()
            val incomeCategory = incomes.find { it.id == incomeCategoryId } ?: incomes.firstOrNull()
            _state.update {
                it.copy(
                    expenseCategories = expenses,
                    selectedExpenseCategory = expenseCategory,
                    incomeCategories = incomes,
                    selectedIncomeCategory = incomeCategory,
                )
            }
        }.launchIn(viewModelScope)
    }

    fun processAction(action: AdvancedSettingAction) {
        viewModelScope.launch {
            when (action) {
                AdvancedSettingAction.ClosePage -> appComposeNavigator.popBackStack()

                AdvancedSettingAction.OpenAccountReOrder ->
                    appComposeNavigator.navigate(ExpenseManagerScreens.AccountReOrderScreen)

                AdvancedSettingAction.DismissDateFilterDialog -> _state.update { it.copy(showDateFilter = false) }

                AdvancedSettingAction.ShowDateFilterDialog -> _state.update { it.copy(showDateFilter = true) }

                AdvancedSettingAction.ToggleAutoOpenCalculator -> {
                    val newValue = !_state.value.isAutoOpenCalculator
                    settingsRepository.setAutoOpenCalculator(newValue)
                    _state.update { it.copy(isAutoOpenCalculator = newValue) }
                }

                is AdvancedSettingAction.SelectAccount -> {
                    val result = settingsRepository.setDefaultAccount(action.account.id)
                    if (result is Resource.Success && result.data) {
                        _state.update { it.copy(selectedAccount = action.account) }
                    }
                }

                is AdvancedSettingAction.SelectExpenseCategory -> {
                    val result = settingsRepository.setDefaultExpenseCategory(action.category.id)
                    if (result is Resource.Success && result.data) {
                        _state.update { it.copy(selectedExpenseCategory = action.category) }
                    }
                }

                is AdvancedSettingAction.SelectIncomeCategory -> {
                    val result = settingsRepository.setDefaultIncomeCategory(action.category.id)
                    if (result is Resource.Success && result.data) {
                        _state.update { it.copy(selectedIncomeCategory = action.category) }
                    }
                }

                // Takes effect the next time the app opens; the current tab doesn't jump.
                is AdvancedSettingAction.SelectLandingPage -> settingsRepository.setLandingPage(action.page)

                AdvancedSettingAction.ToggleCompactSummary -> {
                    val newValue = !_state.value.isCompactSummary
                    settingsRepository.setHomeSummaryCompact(newValue)
                    _state.update { it.copy(isCompactSummary = newValue) }
                }
            }
        }
    }
}
