package com.naveenapps.expensemanager.feature.settings

sealed class SettingAction {

    data object ClosePage : SettingAction()

    data object OpenExport : SettingAction()

    data object OpenRateUs : SettingAction()

    data object OpenAdvancedSettings : SettingAction()

    data object OpenAboutUs : SettingAction()

    data object OpenNotification : SettingAction()

    data object OpenCurrencyEdit : SettingAction()

    data object ShowThemeSelection : SettingAction()

    data object DismissThemeSelection : SettingAction()
}