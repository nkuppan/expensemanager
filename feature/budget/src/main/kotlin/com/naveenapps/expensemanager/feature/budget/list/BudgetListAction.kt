package com.naveenapps.expensemanager.feature.budget.list

sealed class BudgetListAction {

    data object ClosePage : BudgetListAction()

    data object OpenBudgetCreate : BudgetListAction()

    data class EditBudget(val budgetId: String) : BudgetListAction()
}