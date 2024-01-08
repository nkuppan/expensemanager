package com.naveenapps.expensemanager.core.model

import com.naveenapps.expensemanager.core.common.utils.getAmountTextColor

data class AccountUiModel(
    val id: String,
    val name: String,
    val storedIcon: StoredIcon,
    val amount: Amount,
    val amountTextColor: Int,
    val type: AccountType = AccountType.REGULAR,
    val availableCreditLimit: Amount? = null,
)

fun Account.toAccountUiModel(amount: Amount, availableCreditLimit: Amount? = null) = AccountUiModel(
    id = this.id,
    name = this.name,
    storedIcon = this.storedIcon,
    amount = amount,
    type = this.type,
    amountTextColor = this.amount.getAmountTextColor(),
    availableCreditLimit = availableCreditLimit
)
