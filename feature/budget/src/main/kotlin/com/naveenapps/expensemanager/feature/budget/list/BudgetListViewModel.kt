package com.naveenapps.expensemanager.feature.budget.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.budget.GetBudgetsUseCase
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BudgetListViewModel @Inject constructor(
    getBudgetsUseCase: GetBudgetsUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _state = MutableStateFlow(
        BudgetState(
            isLoading = true,
            budgets = emptyList()
        )
    )
    val state = _state.asStateFlow()

    init {
        getBudgetsUseCase.invoke().onEach { budgets ->
            _state.update {
                it.copy(
                    isLoading = false,
                    budgets = budgets
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun openCreateScreen(budgetId: String? = null) {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.BudgetCreate(budgetId),
        )
    }

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    fun processAction(action: BudgetListAction) {
        when (action) {
            BudgetListAction.ClosePage -> closePage()
            is BudgetListAction.EditBudget -> openCreateScreen(action.budgetId)
            BudgetListAction.OpenBudgetCreate -> openCreateScreen()
        }
    }
}
