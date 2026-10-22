package com.naveenapps.expensemanager.core.navigation

import kotlinx.serialization.Serializable

sealed class ExpenseManagerScreens {
    @Serializable
    data object IntroScreen : ExpenseManagerScreens()

    @Serializable
    data object Onboarding : ExpenseManagerScreens()

    @Serializable
    data object Home : ExpenseManagerScreens()

    @Serializable
    data object AccountList : ExpenseManagerScreens()

    @Serializable
    data object CategoryList : ExpenseManagerScreens()

    @Serializable
    data object BudgetList : ExpenseManagerScreens()

    @Serializable
    data object TransactionList : ExpenseManagerScreens()

    @Serializable
    data object Settings : ExpenseManagerScreens()

    @Serializable
    data object CategoryTransaction : ExpenseManagerScreens()

    @Serializable
    data object AnalysisScreen : ExpenseManagerScreens()

    @Serializable
    data object ExportScreen : ExpenseManagerScreens()

    @Serializable
    data object ReminderScreen : ExpenseManagerScreens()

    @Serializable
    data object CurrencyCustomiseScreen : ExpenseManagerScreens()

    @Serializable
    data object AboutUsScreen : ExpenseManagerScreens()

    @Serializable
    data object AdvancedSettingsScreen : ExpenseManagerScreens()

    @Serializable
    data object AccountReOrderScreen : ExpenseManagerScreens()

    @Serializable
    data object RecurringTransactions : ExpenseManagerScreens()

    @Serializable
    data object AutoBackupScreen : ExpenseManagerScreens()

    @Serializable
    data class AccountCreate(val id: String?) : ExpenseManagerScreens()

    @Serializable
    data class CategoryCreate(val id: String?) : ExpenseManagerScreens()

    @Serializable
    data class CategoryDetails(val id: String?) : ExpenseManagerScreens()

    @Serializable
    data class BudgetCreate(val id: String?) : ExpenseManagerScreens()

    @Serializable
    data class BudgetDetails(val id: String?) : ExpenseManagerScreens()

    /**
     * [repeat] pre-selects a repeat frequency (a RecurringFrequency name) for a new transaction,
     * e.g. when adding from the recurring transactions screen. Ignored when editing ([id] set).
     */
    @Serializable
    data class TransactionCreate(val id: String?, val repeat: String? = null) : ExpenseManagerScreens()

    /** Edits a repeating transaction rule (the parent of every future occurrence). */
    @Serializable
    data class RecurringTransactionEdit(val id: String) : ExpenseManagerScreens()
}

object ExpenseManagerArgsNames {
    const val ID: String = "id"
    const val REPEAT: String = "repeat"
}
