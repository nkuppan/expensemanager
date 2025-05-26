package com.naveenapps.expensemanager.feature.budget.create

import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFieldValue
import java.util.Date

data class BudgetCreateState(
    val isLoading: Boolean,
    val name: TextFieldValue<String>,
    val amount: TextFieldValue<String>,
    val month: TextFieldValue<Date>,
    val icon: TextFieldValue<String>,
    val color: TextFieldValue<String>,
    val isAllAccountSelected: Boolean,
    val selectedAccounts: List<AccountUiModel>,
    val isAllCategorySelected: Boolean,
    val selectedCategories: List<Category>,
    val currency: Currency,
    val showDeleteButton: Boolean,
    val showDeleteDialog: Boolean,
    val showAccountSelectionDialog: Boolean,
    val showCategorySelectionDialog: Boolean,
)