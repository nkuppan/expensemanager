package com.naveenapps.expensemanager.feature.settings.advanced

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Category

data class AdvancedSettingState(
    val accounts: List<Account>,
    val selectedAccount: Account?,
    val selectedIncomeCategory: Category?,
    val incomeCategories: List<Category>,
    val selectedExpenseCategory: Category?,
    val expenseCategories: List<Category>,
    val showDateFilter: Boolean
)
