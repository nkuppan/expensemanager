package com.naveenapps.expensemanager.feature.filter

import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.TransactionType

data class FilterState(
    val date: String,
    val dateRangeType: DateRangeType,
    val selectedCategories: List<Category>,
    val selectedAccounts: List<AccountUiModel>,
    val selectedTransactionTypes: List<TransactionType>,
    val showForward: Boolean,
    val showBackward: Boolean,
    val showDateFilter: Boolean,
    val showTypeFilter: Boolean,
)