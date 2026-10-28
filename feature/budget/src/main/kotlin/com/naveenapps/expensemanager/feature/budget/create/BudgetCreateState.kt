package com.naveenapps.expensemanager.feature.budget.create

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFieldValue
import java.util.Date

@Stable
data class BudgetCreateState(
    val isLoading: Boolean,
    /** Optional; empty shows the automatic name, which the field offers as its hint. */
    val name: TextFieldValue<String> = TextFieldValue(value = "", onValueChange = null),
    val amount: TextFieldValue<String>,
    val month: TextFieldValue<Date>,
    val periodType: BudgetPeriod = BudgetPeriod.MONTHLY,
    val isAllAccountSelected: Boolean,
    val selectedAccounts: List<AccountUiModel>,
    val isAllCategorySelected: Boolean,
    val selectedCategories: List<Category>,
    val currency: Currency,
    val showDeleteButton: Boolean,
    val showDeleteDialog: Boolean,
    val showAccountSelectionDialog: Boolean,
    val showCategorySelectionDialog: Boolean,
    val showMonthSelection: Boolean,
)
