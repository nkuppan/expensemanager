package com.naveenapps.expensemanager.feature.account.create

sealed class AccountCreateAction {

    data object ShowDeleteDialog : AccountCreateAction()

    data object DismissDeleteDialog : AccountCreateAction()

    data object ClosePage : AccountCreateAction()

    data object Save : AccountCreateAction()

    data object Delete : AccountCreateAction()
}