package com.naveenapps.expensemanager.feature.transaction.create

import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.TransactionType
import java.util.Date

sealed class TransactionCreateAction {

    data object ClosePage : TransactionCreateAction()

    data object ShowDeleteDialog : TransactionCreateAction()

    data object DismissDeleteDialog : TransactionCreateAction()

    data class OpenAccountCreate(val account: AccountUiModel?) : TransactionCreateAction()

    data class SelectAccount(val account: AccountUiModel) : TransactionCreateAction()

    data object DismissAccountSelection : TransactionCreateAction()

    data class ShowAccountSelection(val type: AccountSelection) : TransactionCreateAction()

    data class OpenCategoryCreate(val category: Category?) : TransactionCreateAction()

    data class SelectCategory(val category: Category) : TransactionCreateAction()

    data object DismissCategorySelection : TransactionCreateAction()

    data object ShowCategorySelection : TransactionCreateAction()

    data object Delete : TransactionCreateAction()

    data object Save : TransactionCreateAction()

    data class SetNumberPadValue(val amount: String?) : TransactionCreateAction()

    data object ShowNumberPad : TransactionCreateAction()

    data object DismissNumberPad : TransactionCreateAction()

    data class ChangeTransactionType(val type: TransactionType) : TransactionCreateAction()

    data class SelectDate(val date: Date) : TransactionCreateAction()

    data object ShowDateSelection : TransactionCreateAction()

    data object DismissDateSelection : TransactionCreateAction()

    data object ShowTimeSelection : TransactionCreateAction()
}