package com.naveenapps.expensemanager.feature.transaction.recurring

sealed class RecurringTransactionsAction {

    data object ClosePage : RecurringTransactionsAction()

    data class Search(val query: String) : RecurringTransactionsAction()

    /** Opens Create Transaction with Repeat pre-set to Monthly. */
    data object OpenCreate : RecurringTransactionsAction()

    data class OpenEdit(val item: RecurringItemUi) : RecurringTransactionsAction()

    data class AskToStop(val item: RecurringItemUi) : RecurringTransactionsAction()

    data object DismissStop : RecurringTransactionsAction()

    data object ConfirmStop : RecurringTransactionsAction()
}
