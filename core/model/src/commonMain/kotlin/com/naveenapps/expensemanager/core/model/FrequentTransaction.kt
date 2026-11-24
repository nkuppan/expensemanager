package com.naveenapps.expensemanager.core.model

/**
 * Details the user keeps logging unchanged, such as "Tea ₹20" from Cash under Food. Offered on
 * the add screen so a repeat entry is one tap.
 */
data class FrequentTransaction(
    val type: TransactionType,
    val categoryId: String,
    val fromAccountId: String,
    val amount: Double,
    val notes: String,
    val useCount: Int,
)
