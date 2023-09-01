package com.nkuppan.expensemanager.feature.transaction.history

import androidx.annotation.DrawableRes
import com.nkuppan.expensemanager.core.model.CategoryType
import com.nkuppan.expensemanager.core.ui.utils.UiText

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
    @DrawableRes val accountIcon: Int,
    val date: String,
)