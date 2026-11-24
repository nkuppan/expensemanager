package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.foundation.toDayKey
import com.naveenapps.expensemanager.core.foundation.toLocalDateIn
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.core.model.isRecurringOccurrence

// Moved from core:model so the model has no Android dependency: the date is formatted for display
// with core:common's DateUtils (SimpleDateFormat). Goes to a DateDisplayFormatter in the
// KMP groundwork (C6).
fun Transaction.toTransactionUIModel(amount: Amount): TransactionUiItem = TransactionUiItem(
    this.id,
    amount,
    this.notes,
    this.category.name,
    this.type,
    categoryIcon = this.category.storedIcon,
    date = this.createdOn.toLocalDateIn().toDayKey(),
    fromAccountName = this.fromAccount.name,
    fromAccountIcon = this.fromAccount.storedIcon,
    toAccountName = this.toAccount?.name,
    toAccountIcon = this.toAccount?.storedIcon,
    categoryDefaultKey = this.category.defaultKey,
    isRecurring = this.isRecurringOccurrence,
)
