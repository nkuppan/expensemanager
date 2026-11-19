package com.naveenapps.expensemanager.core.navigation

import kotlinx.serialization.Serializable

sealed class ExpenseManagerScreens {

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

    /** About → Open source licences: every bundled library (our own screen, not Google's). */
    @Serializable
    data object OpenSourceLicenses : ExpenseManagerScreens()

    @Serializable
    data class OpenSourceLicenseDetail(val name: String) : ExpenseManagerScreens()

    @Serializable
    data object AdvancedSettingsScreen : ExpenseManagerScreens()

    @Serializable
    data object CurrencyApiProfileScreen : ExpenseManagerScreens()

    @Serializable
    data object ExchangeMethodScreen : ExpenseManagerScreens()

    @Serializable
    data object AccountReOrderScreen : ExpenseManagerScreens()

    /** Notification centre from Home's bell: release notes and pushed insights. */
    @Serializable
    data object NotificationCentre : ExpenseManagerScreens()

    /** Customise dashboard: drag Home's sections into a new order. */
    @Serializable
    data object DashboardReOrder : ExpenseManagerScreens()

    @Serializable
    data object RecurringTransactions : ExpenseManagerScreens()

    /** The Backup page: back up now, folder, automatic backup and recent backups. */
    @Serializable
    data object AutoBackupScreen : ExpenseManagerScreens()

    @Serializable
    data object RestoreScreen : ExpenseManagerScreens()

    @Serializable
    data class AccountCreate(val id: String?) : ExpenseManagerScreens()

    @Serializable
    data class CategoryCreate(val id: String?) : ExpenseManagerScreens()

    @Serializable
    data class CategoryDetails(val id: String?) : ExpenseManagerScreens()

    /** [period] (a BudgetPeriod name) preselects Monthly or Annual for a new budget. */
    @Serializable
    data class BudgetCreate(val id: String?, val period: String? = null) : ExpenseManagerScreens()

    @Serializable
    data class BudgetDetails(val id: String?) : ExpenseManagerScreens()

    /**
     * [repeat] pre-selects a repeat frequency (a RecurringFrequency name) for a new transaction,
     * e.g. when adding from the recurring transactions screen. Ignored when editing ([id] set).
     */
    @Serializable
    data class TransactionCreate(
        val id: String?,
        val repeat: String? = null,
        // Opened from the system share sheet: prefill from the shared receipt.
        val fromShare: Boolean = false,
    ) : ExpenseManagerScreens()

    /** Edits a repeating transaction rule (the parent of every future occurrence). */
    @Serializable
    data class RecurringTransactionEdit(val id: String) : ExpenseManagerScreens()

    /** People & debts: everyone the user lent to or borrowed from, with open balances. */
    @Serializable
    data object DebtList : ExpenseManagerScreens()

    /** One person's debts and repayments. */
    @Serializable
    data class DebtPerson(val id: String) : ExpenseManagerScreens()

    /**
     * Adds or edits a debt entry. [id] edits an existing one. Otherwise [personId] (optional)
     * picks the person, [kind] (a DebtEntryKind name) the starting kind, and [settle] fills in
     * the person's whole open balance as a repayment.
     */
    @Serializable
    data class DebtEntry(
        val id: String? = null,
        val personId: String? = null,
        val kind: String? = null,
        val settle: Boolean = false,
    ) : ExpenseManagerScreens()
}

object ExpenseManagerArgsNames {
    const val ID: String = "id"
    const val REPEAT: String = "repeat"
    const val FROM_SHARE: String = "fromShare"
    const val LICENSE_NAME: String = "name"
    const val PERIOD: String = "period"
    const val PERSON_ID: String = "personId"
    const val KIND: String = "kind"
    const val SETTLE: String = "settle"
}
