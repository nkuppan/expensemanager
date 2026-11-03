package com.naveenapps.expensemanager.feature.dashboard

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.AutoBackupError
import com.naveenapps.expensemanager.core.model.CategoryTransactionState
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.ExpenseFlowState
import com.naveenapps.expensemanager.core.model.TransactionUiItem

@Stable
data class DashboardState(
    val transactionPeriod: String,
    val expenseFlowState: ExpenseFlowState,
    val transactions: List<TransactionUiItem>,
    val homeBudgets: HomeBudgets = HomeBudgets(),
    val accounts: List<AccountUiModel>,
    /** Spending by category for the selected period (the categories card's Expense side). */
    val categoryTransactionState: CategoryTransactionState,
    /** Income by category for the selected period (the categories card's Income side). */
    val incomeCategoryTransactionState: CategoryTransactionState = CategoryTransactionState(
        totalAmount = Amount(0.0),
        pieChartData = emptyList(),
        categoryTransactions = emptyList(),
        categoryType = CategoryType.INCOME,
    ),
    val isCompactSummary: Boolean = false,
    /**
     * False only for a brand-new user who has never saved a transaction. Defaults to true so
     * the first-run card never flashes up for existing users while the count loads.
     */
    val hasCreatedTransaction: Boolean = true,
    /** Lifetime transactions created; the "Get started" checklist is only for new users. */
    val transactionCount: Int = 0,
    val hasCurrentMonthBudget: Boolean = false,
    // Defaults to true so the checklist never flashes in before the stored value loads.
    val isGettingStartedDismissed: Boolean = true,
    /** How the summary totals treated amounts in other currencies, or null if there were none. */
    val conversionNote: ConversionNote? = null,
    /** Set while automatic backups are failing and the warning isn't snoozed. */
    val backupWarning: AutoBackupError? = null,
)

enum class ConversionNote {
    /** Converted at today's fetched rate (not the rate at the time). */
    CURRENT_RATES,

    /** Converted at the user's own fixed rates. */
    FIXED_RATES,

    /** Conversion is disabled: each currency's total is shown on its own. */
    NOT_CONVERTED,
}
