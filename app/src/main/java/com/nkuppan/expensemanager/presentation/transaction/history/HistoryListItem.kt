package com.nkuppan.expensemanager.presentation.transaction.history

import com.nkuppan.expensemanager.common.ui.utils.UiText
import com.nkuppan.expensemanager.domain.model.TransactionType

class HistoryListItem(
    val text: UiText? = null,
    val totalAmount: UiText = UiText.DynamicString(""),
    var transaction: List<TransactionUIModel> = emptyList(),
    var expanded: Boolean = false,
)

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