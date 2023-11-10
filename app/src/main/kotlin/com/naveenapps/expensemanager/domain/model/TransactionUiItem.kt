package com.naveenapps.expensemanager.domain.model

import com.naveenapps.expensemanager.core.common.utils.toCompleteDate
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.ui.utils.UiText
import com.naveenapps.expensemanager.ui.utils.getCurrency


data class TransactionUiState(
    val date: String,
    val amountTextColor: Int,
    val totalAmount: UiText,
    val transactions: List<TransactionUiItem>,
    val isLastItem: Boolean = false
)

data class TransactionUiItem(
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

fun Transaction.toTransactionUIModel(currency: Currency): TransactionUiItem {
    return TransactionUiItem(
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