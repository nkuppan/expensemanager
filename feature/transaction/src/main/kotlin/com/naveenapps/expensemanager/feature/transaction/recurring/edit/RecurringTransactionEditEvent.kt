package com.naveenapps.expensemanager.feature.transaction.recurring.edit

import androidx.annotation.StringRes

sealed class RecurringTransactionEditEvent {
    data class ShowMessage(@StringRes val messageRes: Int) : RecurringTransactionEditEvent()
}
