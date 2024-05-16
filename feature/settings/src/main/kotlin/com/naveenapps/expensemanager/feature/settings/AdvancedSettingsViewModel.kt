package com.naveenapps.expensemanager.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdvancedSettingsViewModel @Inject constructor(
    getAllCategoryUseCase: GetAllCategoryUseCase,
    getAllAccountsUseCase: GetAllAccountsUseCase,
    private val settingsRepository: SettingsRepository,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts = _accounts.asStateFlow()

    private val _selectedAccount = MutableStateFlow<Account?>(null)
    val selectedAccount = _selectedAccount.asStateFlow()

    private val _expenseCategories = MutableStateFlow<List<Category>>(emptyList())
    val expenseCategories = _expenseCategories.asStateFlow()

    private val _selectedExpenseCategory = MutableStateFlow<Category?>(null)
    val selectedExpenseCategory = _selectedExpenseCategory.asStateFlow()

    private val _incomeCategories = MutableStateFlow<List<Category>>(emptyList())
    val incomeCategories = _incomeCategories.asStateFlow()

    private val _selectedIncomeCategory = MutableStateFlow<Category?>(null)
    val selectedIncomeCategory = _selectedIncomeCategory.asStateFlow()

    init {
        getAllAccountsUseCase.invoke().onEach { accounts ->
            _accounts.value = accounts

            val accountId = settingsRepository.getDefaultAccount().firstOrNull()
            val account = accounts.find { it.id == accountId }
            _selectedAccount.value = account ?: accounts.firstOrNull()
        }.launchIn(viewModelScope)

        getAllCategoryUseCase.invoke().onEach { categories ->
            val (expenses, incomes) = categories.partition { category -> category.type.isExpense() }
            _expenseCategories.value = expenses
            _incomeCategories.value = incomes

            val expenseCategoryId = settingsRepository.getDefaultExpenseCategory().firstOrNull()
            val expenseCategory = expenses.find { it.id == expenseCategoryId }
            _selectedExpenseCategory.value = expenseCategory ?: expenses.firstOrNull()

            val incomeCategoryId = settingsRepository.getDefaultIncomeCategory().firstOrNull()
            val incomeCategory = incomes.find { it.id == incomeCategoryId }
            _selectedIncomeCategory.value = incomeCategory ?: incomes.firstOrNull()
        }.launchIn(viewModelScope)
    }

    private fun changeDefaultAccount(account: Account) {
        viewModelScope.launch {
            _selectedAccount.value = account
            account.let {
                settingsRepository.setDefaultAccount(it.id)
            }
        }
    }

    private fun changeSelectedExpenseCategory(category: Category) {
        viewModelScope.launch {
            _selectedExpenseCategory.value = category
            category.let {
                settingsRepository.setDefaultExpenseCategory(it.id)
            }
        }
    }

    private fun changeSelectedIncomeCategory(category: Category) {
        viewModelScope.launch {
            _selectedIncomeCategory.value = category
            category.let {
                settingsRepository.setDefaultIncomeCategory(it.id)
            }
        }
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }

    fun onItemSelection(item: Any) {
        when (item) {
            is Account -> {
                changeDefaultAccount(item)
            }

            is Category -> {
                if (item.type.isExpense()) {
                    changeSelectedExpenseCategory(item)
                } else {
                    changeSelectedIncomeCategory(item)
                }
            }
        }
    }

    fun openAccountsReOrder() {
        appComposeNavigator.navigate(ExpenseManagerScreens.AccountReOrderScreen)
    }
}
