package com.naveenapps.expensemanager.feature.budget.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUiModel
import com.naveenapps.expensemanager.core.domain.usecase.budget.GetBudgetDetailUseCase
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens.BudgetDetails.KEY_BUDGET_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class BudgetDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getBudgetDetailUseCase: GetBudgetDetailUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {
    var budget = MutableStateFlow<BudgetUiModel?>(null)
        private set

    init {
        savedStateHandle.get<String>(KEY_BUDGET_ID)?.let {
            getBudgetDetailUseCase.invoke(it).onEach { updatedBudget ->
                budget.value = updatedBudget
            }.launchIn(viewModelScope)
        }
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }

    fun openTransactionCreateScreen(transactionId: String? = null) {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.TransactionCreate.createRoute(transactionId ?: ""),
        )
    }

    fun openBudgetCreateScreen() {
        budget.value?.id?.let {
            appComposeNavigator.navigate(ExpenseManagerScreens.BudgetCreate.createRoute(it))
        }
    }
}
