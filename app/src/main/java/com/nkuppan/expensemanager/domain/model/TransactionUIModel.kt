package com.nkuppan.expensemanager.domain.model

import com.nkuppan.expensemanager.data.utils.toCompleteDate
import com.nkuppan.expensemanager.ui.utils.UiText
import com.nkuppan.expensemanager.ui.utils.getCurrency

data class TransactionUIModel(
    val id: String,
    val amount: UiText = UiText.DynamicString(""),
    val notes: UiText?,
    val categoryName: String,
    val transactionType: TransactionType,
    val categoryBackgroundColor: String,
    val categoryIcon: String,
    val date: String,
    val fromAccountName: String,
    val fromAccountIcon: String,
    val fromAccountColor: String,
    val toAccountName: String? = null,
    val toAccountIcon: String? = null,
    val toAccountColor: String? = null,
)

fun Transaction.toTransactionUIModel(currency: Currency): TransactionUIModel {
    return TransactionUIModel(
        this.id,
        getCurrency(currency, this.amount),
        if (this.notes.isBlank()) {
            null
        } else {
            UiText.DynamicString(this.notes)
        },
        this.category.name,
        this.type,
        this.category.iconBackgroundColor,
        this.category.iconName,
        date = this.createdOn.toCompleteDate(),
        fromAccountName = this.fromAccount.name,
        fromAccountIcon = this.fromAccount.iconName,
        fromAccountColor = this.fromAccount.iconBackgroundColor,
        toAccountName = this.toAccount?.name,
        toAccountIcon = this.toAccount?.iconName,
        toAccountColor = this.toAccount?.iconBackgroundColor,
    )
}