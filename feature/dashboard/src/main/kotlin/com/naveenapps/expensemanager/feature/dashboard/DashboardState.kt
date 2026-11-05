package com.naveenapps.expensemanager.feature.dashboard

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.AutoBackupError
import com.naveenapps.expensemanager.core.model.CategoryTransactionState
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.ExpenseFlowState
import com.naveenapps.expensemanager.core.model.RecurringTransaction
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
    /** Set while automatic backups are failing and the warning isn't snoozed. */
    val backupWarning: AutoBackupError? = null,
    /** The "Upcoming" card: planned recurring entries, below the transactions. */
    val upcoming: UpcomingUi = UpcomingUi(),
    /**
     * Home's share banner is hidden until this time (epoch millis). Defaults to "never" so it
     * can't flash up before the stored value loads.
     */
    val shareBannerHiddenUntil: Long = Long.MAX_VALUE,
) {
    /** The bottom share banner: for users who've logged a few expenses, unless closed or shared. */
    fun showShareBanner(now: Long): Boolean = transactionCount >= SHARE_BANNER_MIN_TRANSACTIONS && now >= shareBannerHiddenUntil
}

/** Recommendations land better from people who've actually used the app for a while. */
internal const val SHARE_BANNER_MIN_TRANSACTIONS = 5

/** The next few recurring rules, soonest first, and what all rules add up to over [windowDays]. */
@Stable
data class UpcomingUi(
    val items: List<UpcomingItemUi> = emptyList(),
    /** Formatted totals over the window; null when nothing of that type is due. */
    val expenseDue: String? = null,
    val incomeDue: String? = null,
    val windowDays: Int = 30,
)

@Stable
data class UpcomingItemUi(
    val rule: RecurringTransaction,
    val amount: String,
)
