package com.naveenapps.expensemanager.feature.category.details

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.model.CategoryTransaction
import com.naveenapps.expensemanager.core.model.TransactionUiItem

@Stable
data class CategoryDetailsState(
    val categoryTransaction: CategoryTransaction?,
    val transactions: List<TransactionUiItem>
)