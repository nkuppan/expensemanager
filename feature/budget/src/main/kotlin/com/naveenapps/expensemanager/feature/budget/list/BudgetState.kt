package com.naveenapps.expensemanager.feature.budget.list

import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUiModel

data class BudgetState(
    val isLoading: Boolean,
    val budgets: List<BudgetUiModel>
)
