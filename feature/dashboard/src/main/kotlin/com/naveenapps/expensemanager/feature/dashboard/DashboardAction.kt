package com.naveenapps.expensemanager.feature.dashboard

import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUiModel
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.TransactionUiItem

sealed class DashboardAction {

    data object OpenSettings : DashboardAction()

    data class OpenTransactionEdit(val transaction: TransactionUiItem?) : DashboardAction()

    data object OpenTransactionList : DashboardAction()

    data object OpenBudgetList : DashboardAction()

    data object OpenBudgetCreate : DashboardAction()

    data class OpenBudgetDetails(val budgetUiModel: BudgetUiModel) : DashboardAction()

    data class OpenAccountEdit(val account: AccountUiModel) : DashboardAction()

    data object OpenAccountList : DashboardAction()

    data object OpenReminder : DashboardAction()

    data object DismissGettingStarted : DashboardAction()

    data object RecapShared : DashboardAction()

    data object DismissRecap : DashboardAction()

    /** "Nothing spent today" from the streak card: keeps the streak without a transaction. */
    data object MarkNoSpendToday : DashboardAction()
}
