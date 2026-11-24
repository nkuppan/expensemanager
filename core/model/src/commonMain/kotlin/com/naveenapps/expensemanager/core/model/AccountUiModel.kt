package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable

@Stable
data class AccountUiModel(
    val id: String,
    val name: String,
    val storedIcon: StoredIcon,
    val amount: Amount,
    val amountTone: AmountTone,
    val type: AccountType = AccountType.REGULAR,
    val availableCreditLimit: Amount? = null,
)

fun Account.toAccountUiModel(
    amount: Amount,
    availableCreditLimit: Amount? = null,
) = AccountUiModel(
    id = this.id,
    name = this.name,
    storedIcon = this.storedIcon,
    amount = amount,
    type = this.type,
    amountTone = AmountTone.of(this.amount),
    availableCreditLimit = availableCreditLimit,
)
