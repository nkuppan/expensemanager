package com.naveenapps.expensemanager.core.data.repository.export

import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType

/**
 * Exported files are records, so nothing is converted: totals are reported per currency
 * (income and expense of different currencies are never added together).
 */
internal data class CurrencyTotals(
    val currencyCode: String,
    val income: Double,
    val expense: Double,
) {
    val net: Double get() = income - expense
}

internal fun List<Transaction>.totalsByCurrency(): List<CurrencyTotals> = groupBy { it.fromCurrencyCode }
    .map { (code, items) ->
        CurrencyTotals(
            currencyCode = code,
            income = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.amount },
            expense = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.amount },
        )
    }
    .sortedBy { it.currencyCode }

/** What the destination account received, for a cross-currency transfer; else null. */
internal fun Transaction.receivedAmount(): Double? = rateSnapshot?.let { amount.amount * it.rate }
