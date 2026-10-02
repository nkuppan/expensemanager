package com.naveenapps.expensemanager.feature.dashboard

import com.naveenapps.expensemanager.core.model.LoggingStreak
import com.naveenapps.expensemanager.core.model.MonthlyRecap
import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUiModel
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.CategoryTransactionState
import com.naveenapps.expensemanager.core.model.ExpenseFlowState
import com.naveenapps.expensemanager.core.model.TransactionUiItem

@Stable
data class DashboardState(
    val transactionPeriod: String,
    val expenseFlowState: ExpenseFlowState,
    val transactions: List<TransactionUiItem>,
    val budgets: List<BudgetUiModel>,
    val accounts: List<AccountUiModel>,
    val categoryTransactionState: CategoryTransactionState,
    val isCompactSummary: Boolean = false,
    val showCreateBudgetForMonth: String? = null,
    /**
     * False only for a brand-new user who has never saved a transaction. Defaults to true so
     * the first-run card never flashes up for existing users while the count loads.
     */
    val hasCreatedTransaction: Boolean = true,
    /** Lifetime transactions created; the "Get started" checklist is only for new users. */
    val transactionCount: Int = 0,
    val hasCurrentMonthBudget: Boolean = false,
    val isReminderOn: Boolean = false,
    // Defaults to true so the checklist never flashes in before the stored value loads.
    val isGettingStartedDismissed: Boolean = true,
    /** Last month's recap, shown as a shareable card during the first days of a month. */
    val recap: MonthlyRecap? = null,
    /** Daily logging streak; null until loaded. */
    val streak: LoggingStreak? = null,
)
