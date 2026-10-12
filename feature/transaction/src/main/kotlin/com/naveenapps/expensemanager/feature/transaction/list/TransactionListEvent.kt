package com.naveenapps.expensemanager.feature.transaction.list

import androidx.annotation.StringRes

sealed class TransactionListEvent {

    /** A transaction was deleted; offer to bring it back. */
    data object ShowUndoDelete : TransactionListEvent()

    data class ShowMessage(@StringRes val messageRes: Int) : TransactionListEvent()
}
