package com.naveenapps.expensemanager.feature.transaction.list

sealed class TransactionListAction {

    data object ClosePage : TransactionListAction()

    data object OpenCreateTransaction : TransactionListAction()

    data class OpenEdiTransaction(val transactionId: String) : TransactionListAction()

    data class Search(val query: String) : TransactionListAction()

    data class DeleteTransaction(val transactionId: String) : TransactionListAction()

    /** The user tapped "Undo" on the snackbar shown after a delete. */
    data object UndoDelete : TransactionListAction()

    /** The undo snackbar went away without "Undo": the delete is final. */
    data object UndoExpired : TransactionListAction()
}
