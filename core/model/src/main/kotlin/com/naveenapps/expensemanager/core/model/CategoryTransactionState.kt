package com.naveenapps.expensemanager.core.model

data class CategoryTransactionState(
    val totalAmount: Amount,
    val pieChartData: List<PieChartData>,
    val categoryTransactions: List<CategoryTransaction>,
    val hideValues: Boolean = false,
    val categoryType: CategoryType,
)
