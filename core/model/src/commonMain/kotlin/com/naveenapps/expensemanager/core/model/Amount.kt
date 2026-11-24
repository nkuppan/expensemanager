package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable

@Stable
data class Amount(
    val amount: Double,
    val amountString: String? = null,
    val currency: Currency? = null,
)
