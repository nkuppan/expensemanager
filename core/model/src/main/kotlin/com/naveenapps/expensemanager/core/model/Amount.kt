package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable

@Stable
data class Amount(
    val amount: Double,
    val amountString: String? = null,
    val currency: Currency? = null,
    /**
     * Set when this is a converted total that couldn't be computed (e.g. "USD→SYP"): [amount]
     * is meaningless then, and [amountString] explains the missing rate instead of a number.
     */
    val unavailableRates: List<String> = emptyList(),
    /**
     * Set when conversion is disabled and this total spans several currencies: [amount] is
     * meaningless then, and [amountString] lists each currency's own sum.
     */
    val isPerCurrency: Boolean = false,
) {
    val isAvailable: Boolean get() = unavailableRates.isEmpty() && !isPerCurrency
}
