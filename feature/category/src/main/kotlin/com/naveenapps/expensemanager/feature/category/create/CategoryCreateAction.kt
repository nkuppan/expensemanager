package com.naveenapps.expensemanager.feature.category.create

sealed class CategoryCreateAction {

    data object ClosePage : CategoryCreateAction()

    data object ShowDeleteDialog : CategoryCreateAction()

    data object DismissDeleteDialog : CategoryCreateAction()

    data object Save : CategoryCreateAction()

    data object Delete : CategoryCreateAction()

    data class SelectColor(val color: String) : CategoryCreateAction()

    data class SelectIcon(val icon: String) : CategoryCreateAction()
}