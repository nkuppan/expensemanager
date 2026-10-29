package com.naveenapps.expensemanager.feature.dashboard

import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.model.TransactionUiItem

sealed class DashboardAction {

    data object OpenSettings : DashboardAction()

    data class OpenTransactionEdit(val transaction: TransactionUiItem?) : DashboardAction()

    data object OpenTransactionList : DashboardAction()

    data object OpenBudgetList : DashboardAction()

    /** [period] preselects Monthly or Annual on the create screen. */
    data class OpenBudgetCreate(val period: BudgetPeriod? = null) : DashboardAction()

    data class OpenBudgetDetails(val id: String) : DashboardAction()

    data class EditBudget(val id: String) : DashboardAction()

    data class OpenAccountEdit(val account: AccountUiModel) : DashboardAction()

    data object OpenAccountList : DashboardAction()

    data object OpenCurrency : DashboardAction()

    data object DismissGettingStarted : DashboardAction()

    /** "Fix" on the automatic-backup-failed banner. */
    data object FixAutoBackup : DashboardAction()

    /** "Later" on the banner: hides it for a few days, or until the next fix and failure. */
    data object SnoozeAutoBackupWarning : DashboardAction()
}
