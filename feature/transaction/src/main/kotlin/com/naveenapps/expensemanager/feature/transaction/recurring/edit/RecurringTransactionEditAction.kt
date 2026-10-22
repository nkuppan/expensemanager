package com.naveenapps.expensemanager.feature.transaction.recurring.edit

import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.RecurringFrequency
import com.naveenapps.expensemanager.feature.transaction.create.AccountSelection
import java.util.Date

sealed class RecurringTransactionEditAction {

    data object ClosePage : RecurringTransactionEditAction()

    data object ConfirmDiscard : RecurringTransactionEditAction()

    data object DismissDiscardDialog : RecurringTransactionEditAction()

    data object Save : RecurringTransactionEditAction()

    data object ShowStopDialog : RecurringTransactionEditAction()

    data object DismissStopDialog : RecurringTransactionEditAction()

    data object ConfirmStop : RecurringTransactionEditAction()

    data object ShowNumberPad : RecurringTransactionEditAction()

    /** Null when the keypad was dismissed without a value. */
    data class SetNumberPadValue(val amount: String?) : RecurringTransactionEditAction()

    data class SelectFrequency(val frequency: RecurringFrequency) : RecurringTransactionEditAction()

    data object ShowDateSelection : RecurringTransactionEditAction()

    data object ShowTimeSelection : RecurringTransactionEditAction()

    data object DismissDateSelection : RecurringTransactionEditAction()

    data class SelectDate(val date: Date) : RecurringTransactionEditAction()

    data class SelectTime(val hour: Int, val minute: Int) : RecurringTransactionEditAction()

    data object ShowCategorySelection : RecurringTransactionEditAction()

    data object DismissCategorySelection : RecurringTransactionEditAction()

    data class SelectCategory(val category: Category) : RecurringTransactionEditAction()

    data class ShowAccountSelection(val type: AccountSelection) : RecurringTransactionEditAction()

    data object DismissAccountSelection : RecurringTransactionEditAction()

    data class SelectAccount(val account: AccountUiModel) : RecurringTransactionEditAction()
}
