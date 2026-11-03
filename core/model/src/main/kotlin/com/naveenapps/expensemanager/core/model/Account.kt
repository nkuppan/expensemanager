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
    /** ISO 4217 code, fixed at creation. */
    val currencyCode: String,
    /** Hidden from lists and pickers; kept for the history of its transactions. */
    val isArchived: Boolean = false,
)

fun Account.getAvailableCreditLimit(): Double = creditLimit + amount
