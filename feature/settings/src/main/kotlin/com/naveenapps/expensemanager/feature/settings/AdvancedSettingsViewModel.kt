package com.naveenapps.expensemanager.feature.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    var accounts by mutableStateOf<List<Account>>(emptyList())
        private set

    var selectedAccount by mutableStateOf<Account?>(null)
        private set

    var expenseCategories by mutableStateOf<List<Category>>(emptyList())
        private set

    var selectedExpenseCategory by mutableStateOf<Category?>(null)
        private set

    var incomeCategories by mutableStateOf<List<Category>>(emptyList())
        private set

    var selectedIncomeCategory by mutableStateOf<Category?>(null)
        private set

    init {
        getAllAccountsUseCase.invoke().onEach {
            accounts = it

            val accountId = settingsRepository.getDefaultAccount().firstOrNull()
            val account = it.find { it.id == accountId }
            selectedAccount = account ?: it.firstOrNull()
        }.launchIn(viewModelScope)

        getAllCategoryUseCase.invoke().onEach {
            val (expenses, incomes) = it.partition { category -> category.type.isExpense() }
            expenseCategories = expenses
            incomeCategories = incomes

            val expenseCategoryId = settingsRepository.getDefaultExpenseCategory().firstOrNull()
            val expenseCategory = expenses.find { it.id == expenseCategoryId }
            selectedExpenseCategory = expenseCategory ?: expenses.firstOrNull()

            val incomeCategoryId = settingsRepository.getDefaultIncomeCategory().firstOrNull()
            val incomeCategory = incomes.find { it.id == incomeCategoryId }
            selectedIncomeCategory = incomeCategory ?: incomes.firstOrNull()
        }.launchIn(viewModelScope)
    }

    private fun changeDefaultAccount(account: Account) {
        viewModelScope.launch {
            selectedAccount = account
            selectedAccount?.let {
                settingsRepository.setDefaultAccount(it.id)
            }
        }
    }

    private fun changeSelectedExpenseCategory(category: Category) {
        viewModelScope.launch {
            selectedExpenseCategory = category
            selectedExpenseCategory?.let {
                settingsRepository.setDefaultExpenseCategory(it.id)
            }
        }
    }

    private fun changeSelectedIncomeCategory(category: Category) {
        viewModelScope.launch {
            selectedIncomeCategory = category
            selectedIncomeCategory?.let {
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
}
