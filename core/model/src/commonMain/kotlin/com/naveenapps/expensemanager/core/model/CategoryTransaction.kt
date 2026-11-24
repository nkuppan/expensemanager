package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable

@Stable
data class CategoryTransaction(
    val category: Category,
    val percent: Float,
    val amount: Amount,
    val transaction: List<Transaction> = emptyList(),
)
