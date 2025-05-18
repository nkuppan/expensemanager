package com.naveenapps.expensemanager.feature.settings.advanced

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Category

sealed class AdvancedSettingAction {

    data object OpenAccountReOrder : AdvancedSettingAction()

    data object ClosePage : AdvancedSettingAction()

    data object Backup : AdvancedSettingAction()

    data object Restore : AdvancedSettingAction()

    data class SelectIncomeCategory(val category: Category) : AdvancedSettingAction()

    data class SelectExpenseCategory(val category: Category) : AdvancedSettingAction()

    data class SelectAccount(val account: Account) : AdvancedSettingAction()

    data object ShowDateFilterDialog : AdvancedSettingAction()

    data object DismissDateFilterDialog : AdvancedSettingAction()
}