package com.naveenapps.expensemanager.feature.budget.create

import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Category

sealed class BudgetCreateAction {

    data object ClosePage : BudgetCreateAction()

    data object OpenAccountSelectionDialog : BudgetCreateAction()

    data object CloseAccountSelectionDialog : BudgetCreateAction()

    data class SelectAccounts(
        val isAllSelected: Boolean,
        val accounts: List<AccountUiModel>
    ) : BudgetCreateAction()

    data object OpenCategorySelectionDialog : BudgetCreateAction()

    data object CloseCategorySelectionDialog : BudgetCreateAction()

    data class SelectCategories(
        val isAllSelected: Boolean,
        val categories: List<Category>
    ) : BudgetCreateAction()

    data object OpenDeleteDialog : BudgetCreateAction()

    data object CloseDeleteDialog : BudgetCreateAction()

    data object Save : BudgetCreateAction()

    data object Delete : BudgetCreateAction()
}