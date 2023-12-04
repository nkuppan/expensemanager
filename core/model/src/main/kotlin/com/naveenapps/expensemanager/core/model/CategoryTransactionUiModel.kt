package com.naveenapps.expensemanager.core.model

data class CategoryTransactionUiModel(
    val totalAmount: Amount,
    val pieChartData: List<PieChartData>,
    val categoryTransactions: List<CategoryTransaction>,
    val hideValues: Boolean = false,
)
