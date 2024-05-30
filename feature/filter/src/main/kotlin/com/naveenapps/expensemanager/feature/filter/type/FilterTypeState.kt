package com.naveenapps.expensemanager.feature.filter.type

import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.TransactionType

data class FilterTypeState(
    val categories: List<Category>,
    val selectedCategories: List<Category>,
    val accounts: List<AccountUiModel>,
    val selectedAccounts: List<AccountUiModel>,
    val transactionTypes: List<TransactionType>,
    val selectedTransactionTypes: List<TransactionType>,
)