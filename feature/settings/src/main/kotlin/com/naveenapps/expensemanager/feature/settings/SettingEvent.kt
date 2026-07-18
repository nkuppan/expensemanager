package com.naveenapps.expensemanager.feature.settings

sealed class SettingEvent {

    data object RateUs : SettingEvent()

    // Moved here from AdvancedSettingEvent along with the Data & Backup section.
    data object Backup : SettingEvent()

    data object Restore : SettingEvent()
}