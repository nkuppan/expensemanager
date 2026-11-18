package com.naveenapps.expensemanager.feature.settings.advanced

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.LandingPage

sealed class AdvancedSettingAction {

    data object OpenAccountReOrder : AdvancedSettingAction()

    data object ClosePage : AdvancedSettingAction()

    data object ShowDateFilterDialog : AdvancedSettingAction()

    data object DismissDateFilterDialog : AdvancedSettingAction()

    data object ToggleAutoOpenCalculator : AdvancedSettingAction()

    data class SelectAccount(val account: Account) : AdvancedSettingAction()

    data class SelectExpenseCategory(val category: Category) : AdvancedSettingAction()

    data class SelectIncomeCategory(val category: Category) : AdvancedSettingAction()

    data object ToggleCompactSummary : AdvancedSettingAction()

    data class SelectLandingPage(val page: LandingPage) : AdvancedSettingAction()
}
