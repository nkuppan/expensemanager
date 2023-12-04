package com.naveenapps.expensemanager.core.model

data class Amount(
    val amount: Double,
    val amountString: String? = null,
    val currency: Currency? = null,
)
