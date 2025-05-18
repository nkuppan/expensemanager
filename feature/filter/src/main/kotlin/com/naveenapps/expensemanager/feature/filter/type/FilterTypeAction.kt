package com.naveenapps.expensemanager.feature.filter.type

import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.TransactionType

sealed class FilterTypeAction {

    data class SelectAccount(val account: AccountUiModel) : FilterTypeAction()

    data class SelectCategory(val category: Category) : FilterTypeAction()

    data class SelectTransactionType(val transactionType: TransactionType) : FilterTypeAction()

    data object SaveChanges : FilterTypeAction()
}