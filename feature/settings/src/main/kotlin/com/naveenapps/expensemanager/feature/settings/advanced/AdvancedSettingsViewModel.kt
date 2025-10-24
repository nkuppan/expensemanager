package com.naveenapps.expensemanager.feature.settings.advanced

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
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


class AdvancedSettingsViewModel(
    getAllCategoryUseCase: GetAllCategoryUseCase,
    getAllAccountsUseCase: GetAllAccountsUseCase,
    private val settingsRepository: SettingsRepository,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _event = Channel<AdvancedSettingEvent>()
    val event = _event.receiveAsFlow()

    private val _state = MutableStateFlow(
        AdvancedSettingState(
            accounts = emptyList(),
            selectedAccount = null,
            incomeCategories = emptyList(),
            selectedIncomeCategory = null,
            expenseCategories = emptyList(),
            selectedExpenseCategory = null,
            showDateFilter = false,
        )
    )
    val state = _state.asStateFlow()

    init {
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
                    incomeCategories = expenses,
                    selectedIncomeCategory = incomeCategory ?: incomes.firstOrNull(),
                )
            }
        }.launchIn(viewModelScope)
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

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    private fun openAccountsReOrder() {
        appComposeNavigator.navigate(ExpenseManagerScreens.AccountReOrderScreen)
    }

    fun processAction(action: AdvancedSettingAction) {
        viewModelScope.launch {
            when (action) {
                AdvancedSettingAction.Backup -> {
                    _event.send(AdvancedSettingEvent.Backup)
                }

                AdvancedSettingAction.ClosePage -> {
                    closePage()
                }

                AdvancedSettingAction.OpenAccountReOrder -> {
                    openAccountsReOrder()
                }

                AdvancedSettingAction.Restore -> {
                    _event.send(AdvancedSettingEvent.Restore)
                }

                is AdvancedSettingAction.SelectAccount -> {
                    changeDefaultAccount(action.account)
                }

                is AdvancedSettingAction.SelectExpenseCategory -> {
                    changeSelectedExpenseCategory(action.category)
                }

                is AdvancedSettingAction.SelectIncomeCategory -> {
                    changeSelectedIncomeCategory(action.category)
                }

                AdvancedSettingAction.DismissDateFilterDialog -> {
                    _state.update { it.copy(showDateFilter = false) }
                }

                AdvancedSettingAction.ShowDateFilterDialog -> {
                    _state.update { it.copy(showDateFilter = true) }
                }
            }
        }
    }
}
