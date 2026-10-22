package com.naveenapps.expensemanager.feature.settings

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Category

sealed class SettingAction {

    data object ClosePage : SettingAction()

    data object OpenExport : SettingAction()

    data object OpenRateUs : SettingAction()

    data object ShareApp : SettingAction()

    data object OpenAdvancedSettings : SettingAction()

    data object OpenAboutUs : SettingAction()

    data object OpenNotification : SettingAction()

    data object OpenCurrencyEdit : SettingAction()

    data object ShowThemeSelection : SettingAction()

    data object DismissThemeSelection : SettingAction()

    data object ShowLanguageSelection : SettingAction()

    data object DismissLanguageSelection : SettingAction()

    // Defaults section (moved here from AdvancedSettingAction)
    data class SelectAccount(val account: Account) : SettingAction()

    data class SelectExpenseCategory(val category: Category) : SettingAction()

    data class SelectIncomeCategory(val category: Category) : SettingAction()

    data object ToggleCompactSummary : SettingAction()

    // Data & Backup section
    data object OpenRecurringTransactions : SettingAction()

    data object OpenAutoBackup : SettingAction()

    data object Backup : SettingAction()

    data object Restore : SettingAction()

    /** Result of the "create document" picker; null when the user backed out. */
    data class BackupDestinationSelected(val uri: String?) : SettingAction()

    data object ConfirmRestore : SettingAction()

    data object DismissRestoreConfirmation : SettingAction()

    /** Result of the "open document" picker; null when the user backed out. */
    data class RestoreSourceSelected(val uri: String?) : SettingAction()

    /** Confirms the passphrase dialog; an empty value on backup means "no passphrase". */
    data class SubmitPassphrase(val passphrase: String) : SettingAction()

    data object DismissPassphrasePrompt : SettingAction()

    // Security section
    data object ToggleAppLock : SettingAction()

    // Developer section (debug builds only)
    data object LoadDemoData : SettingAction()

    data object ConfirmDemoData : SettingAction()

    data object DismissDemoData : SettingAction()
}
