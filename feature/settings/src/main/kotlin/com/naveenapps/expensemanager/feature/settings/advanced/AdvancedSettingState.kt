package com.naveenapps.expensemanager.feature.settings.advanced

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.LandingPage

// Settings → More → Preferences: everything set once and rarely changed (defaults for new
// entries, Home, the default period filter, account order), so the main Settings screen stays
// short.
data class AdvancedSettingState(
    val showDateFilter: Boolean = false,
    val isAutoOpenCalculator: Boolean = true,
    val accounts: List<Account> = emptyList(),
    val selectedAccount: Account? = null,
    val expenseCategories: List<Category> = emptyList(),
    val selectedExpenseCategory: Category? = null,
    val incomeCategories: List<Category> = emptyList(),
    val selectedIncomeCategory: Category? = null,
    val isCompactSummary: Boolean = false,
    /** Bottom-bar tab the app opens on. */
    val landingPage: LandingPage = LandingPage.HOME,
)
