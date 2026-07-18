package com.naveenapps.expensemanager.feature.settings.advanced

sealed class AdvancedSettingAction {

    data object OpenAccountReOrder : AdvancedSettingAction()

    data object ClosePage : AdvancedSettingAction()

    data object ShowDateFilterDialog : AdvancedSettingAction()

    data object DismissDateFilterDialog : AdvancedSettingAction()
}