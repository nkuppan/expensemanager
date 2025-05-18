package com.naveenapps.expensemanager.feature.settings.advanced

sealed class AdvancedSettingEvent {

    data object Backup : AdvancedSettingEvent()

    data object Restore : AdvancedSettingEvent()
}