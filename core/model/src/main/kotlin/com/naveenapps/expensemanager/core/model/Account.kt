package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable
import java.util.Date

@Stable
data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val storedIcon: StoredIcon,
    val createdOn: Date,
    val updatedOn: Date,
    val sequence: Int = Int.MAX_VALUE,
    val amount: Double = 0.0,
    val creditLimit: Double = 0.0,
)

fun Account.getAvailableCreditLimit(): Double {
    return creditLimit + amount
}
