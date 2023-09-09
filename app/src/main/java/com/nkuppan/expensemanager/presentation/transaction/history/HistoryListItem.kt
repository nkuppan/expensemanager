package com.nkuppan.expensemanager.presentation.transaction.history

import androidx.annotation.DrawableRes
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.domain.model.CategoryType

class HistoryListItem(
    val text: UiText? = null,
    val totalAmount: UiText = UiText.DynamicString(""),
    var transaction: List<TransactionUIModel> = emptyList(),
    var expanded: Boolean = false,
    var type: ItemType = ItemType.PARENT,
)

data class TransactionUIModel(
    val id: String,
    val amount: UiText = UiText.DynamicString(""),
    val notes: UiText = UiText.DynamicString(""),
    val categoryName: String,
    val categoryType: CategoryType,
    val categoryBackgroundColor: String,
    val accountName: String,
    @DrawableRes val accountIcon: Int,
    val date: String,
)