package com.naveenapps.expensemanager.feature.category.details

sealed class CategoryDetailsAction {

    data object ClosePage : CategoryDetailsAction()

    data object OpenCategoryEdit : CategoryDetailsAction()

    data object OpenTransactionCreate : CategoryDetailsAction()

    data class OpenTransactionEdit(val id: String) : CategoryDetailsAction()
}