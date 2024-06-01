package com.naveenapps.expensemanager.feature.account.reorder

sealed class AccountReOrderAction {

    data object ClosePage : AccountReOrderAction()

    data object Save : AccountReOrderAction()

    data class Swap(val fromIndex: Int, val toIndex: Int) : AccountReOrderAction()
}