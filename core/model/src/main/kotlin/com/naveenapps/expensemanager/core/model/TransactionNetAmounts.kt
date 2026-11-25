package com.naveenapps.expensemanager.core.model

/**
 * Net spending contribution of one transaction, for every aggregate that shows how much was
 * spent: an expense adds and a refund subtracts (the money came back). Income and transfers add
 * nothing here. A refund reduces the spending of the period it was recorded in, so the period of
 * the original purchase keeps its historical figure.
 */
fun Transaction.netExpenseAmount(): Double = when {
    type.isExpense() -> amount.amount
    type.isRefund() -> -amount.amount
    else -> 0.0
}

/** Net income contribution: income adds, everything else (transfers, refunds) adds nothing. */
fun Transaction.netIncomeAmount(): Double = if (type.isIncome()) amount.amount else 0.0

/** Net spending of a list: expenses minus refunds. */
fun List<Transaction>.netExpense(): Double = sumOf { it.netExpenseAmount() }

/** Net income of a list. */
fun List<Transaction>.netIncome(): Double = sumOf { it.netIncomeAmount() }
