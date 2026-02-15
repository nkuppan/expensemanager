package com.naveenapps.expensemanager.feature.dashboard

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUiModel
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.CategoryTransactionState
import com.naveenapps.expensemanager.core.model.ExpenseFlowState
import com.naveenapps.expensemanager.core.model.TransactionUiItem

@Stable
data class DashboardState(
    val expenseFlowState: ExpenseFlowState,
    val transactions: List<TransactionUiItem>,
    val budgets: List<BudgetUiModel>,
    val accounts: List<AccountUiModel>,
    val categoryTransactionState: CategoryTransactionState
)