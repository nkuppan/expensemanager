package com.naveenapps.expensemanager.core.model


data class AccountUiModel(
    val id: String,
    val name: String,
    val storedIcon: StoredIcon,
    val amount: Amount,
    val amountTextColor: Int,
    val type: AccountType = AccountType.REGULAR
)