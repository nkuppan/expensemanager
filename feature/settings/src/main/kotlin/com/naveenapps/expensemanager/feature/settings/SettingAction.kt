package com.naveenapps.expensemanager.feature.settings

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

    // Data & Backup section
    data object OpenRecurringTransactions : SettingAction()

    /** Backup & restore: the backup screen, which also links to restoring. */
    data object OpenBackup : SettingAction()

    // Security section
    data object ToggleAppLock : SettingAction()

    data object ToggleScreenshotBlock : SettingAction()

    // Developer section (debug builds only)
    data object LoadDemoData : SettingAction()

    data object ConfirmDemoData : SettingAction()

    data object DismissDemoData : SettingAction()
}
