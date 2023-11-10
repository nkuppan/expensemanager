package com.naveenapps.expensemanager.presentation.budget.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.UiState
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.domain.usecase.budget.BudgetUiModel
import com.naveenapps.expensemanager.domain.usecase.budget.GetBudgetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class BudgetListViewModel @Inject constructor(
    getBudgetsUseCase: GetBudgetsUseCase,
) : ViewModel() {

    private val _budgets = MutableStateFlow<UiState<List<BudgetUiModel>>>(UiState.Loading)
    val budgets = _budgets.asStateFlow()

    init {
        getBudgetsUseCase.invoke().onEach {
            _budgets.value = if (it.isEmpty()) {
                UiState.Empty
            } else {
                UiState.Success(it)
            }
        }.launchIn(viewModelScope)
    }
}

fun List<Transaction>.toTransactionSum() =
    this.sumOf {
        when (it.type) {
            TransactionType.INCOME -> {
                it.amount
            }

            TransactionType.EXPENSE -> {
                it.amount * -1
            }

            TransactionType.TRANSFER -> {
                0.0
            }
        }
    }
