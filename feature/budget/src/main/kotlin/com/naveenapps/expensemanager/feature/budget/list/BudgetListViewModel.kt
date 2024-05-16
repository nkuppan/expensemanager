package com.naveenapps.expensemanager.feature.budget.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.UiState
import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUiModel
import com.naveenapps.expensemanager.core.domain.usecase.budget.GetBudgetsUseCase
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class BudgetListViewModel @Inject constructor(
    getBudgetsUseCase: GetBudgetsUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    var budgets = MutableStateFlow<UiState<List<BudgetUiModel>>>(UiState.Loading)
        private set

    init {
        getBudgetsUseCase.invoke().onEach {
            budgets.value = if (it.isEmpty()) {
                UiState.Empty
            } else {
                UiState.Success(it)
            }
        }.launchIn(viewModelScope)
    }

    fun openCreateScreen(model: BudgetUiModel? = null) {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.BudgetCreate(model?.id),
        )
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }
}
