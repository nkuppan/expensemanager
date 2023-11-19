package com.naveenapps.expensemanager.feature.budget.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUiModel
import com.naveenapps.expensemanager.core.domain.usecase.budget.GetBudgetDetailUseCase
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getBudgetDetailUseCase: GetBudgetDetailUseCase,
    private val appComposeNavigator: AppComposeNavigator
) : ViewModel() {

    private val _budget = MutableStateFlow<BudgetUiModel?>(null)
    val budget = _budget.asStateFlow()

    init {
        loadBudget(savedStateHandle.get<String>(ExpenseManagerScreens.BudgetDetails.KEY_BUDGET_ID))
    }

    private fun loadBudget(budgetId: String?) {
        budgetId ?: return
        viewModelScope.launch {
            when (val response = getBudgetDetailUseCase.invoke(budgetId)) {
                is Resource.Error -> {

                }

                is Resource.Success -> {
                    _budget.value = response.data
                }
            }
        }
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }

    fun openTransactionCreateScreen(transactionId: String?) {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.TransactionCreate.createRoute(transactionId ?: "")
        )
    }

    fun openBudgetCreateScreen(budgetId: String) {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.BudgetCreate.createRoute(budgetId)
        )
    }
}