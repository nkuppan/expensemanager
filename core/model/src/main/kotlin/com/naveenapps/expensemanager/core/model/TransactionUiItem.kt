package com.naveenapps.expensemanager.core.model

import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate

data class TransactionGroup(
    val date: String,
    val amountTextColor: Int,
    val totalAmount: Amount,
    val transactions: List<TransactionUiItem>,
    val isLastItem: Boolean = false,
)

data class TransactionUiItem(
    val id: String,
    val amount: Amount = Amount(0.0),
    val notes: String?,
    val categoryName: String,
    val transactionType: TransactionType,
    val categoryIcon: StoredIcon,
    val date: String,
    val fromAccountName: String,
    val fromAccountIcon: StoredIcon,
    val toAccountName: String? = null,
    val toAccountIcon: StoredIcon? = null,
)

fun Transaction.toTransactionUIModel(amount: Amount): TransactionUiItem {
    return TransactionUiItem(
        this.id,
        amount,
        this.notes,
        this.category.name,
        this.type,
        categoryIcon = this.category.storedIcon,
        date = this.createdOn.toCompleteDateWithDate(),
        fromAccountName = this.fromAccount.name,
        fromAccountIcon = this.fromAccount.storedIcon,
        toAccountName = this.toAccount?.name,
        toAccountIcon = this.toAccount?.storedIcon,
    )
}
