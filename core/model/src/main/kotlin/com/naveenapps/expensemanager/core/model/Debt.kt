package com.naveenapps.expensemanager.core.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date

/** Someone the user lends money to or borrows from. */
data class DebtPerson(
    val id: String,
    val name: String,
    val createdOn: Date,
    val updatedOn: Date,
)

/**
 * What happened between the user and a person. Each kind moves the person's balance one way
 * (positive balance = they owe the user) and, when an account is linked, that account the
 * opposite way: lending ₹2,000 from Cash means they owe ₹2,000 more and Cash has ₹2,000 less.
 */
enum class DebtEntryKind(
    /** +1 when the entry makes the person owe the user more (or the user owe them less). */
    val balanceSign: Int,
) {
    /** The user gave them money. */
    LENT(1),

    /** The user got money from them. */
    BORROWED(-1),

    /** They paid the user back. */
    RECEIVED(-1),

    /** The user paid them back. */
    PAID(1),
    ;

    /** How a linked account moves: always the opposite of the person's balance. */
    val accountSign: Int get() = -balanceSign

    /** Lent / borrowed start a debt (and may have a due date); received / paid settle one. */
    val isNewDebt: Boolean get() = this == LENT || this == BORROWED
}

/**
 * One entry in the ledger. [amountMilli] is exact: thousandths of the currency unit, so
 * ₹20.50 is 20500 and "lent ₹500, received ₹500" is exactly zero. Never shown to analytics.
 */
data class DebtEntry(
    val id: String,
    val personId: String,
    val kind: DebtEntryKind,
    val amountMilli: Long,
    /** The account the money came from or went to; null when it didn't touch an account. */
    val accountId: String?,
    val date: Date,
    /** When it should be paid back (lent / borrowed only). */
    val dueDate: Date?,
    val note: String,
    val createdOn: Date,
    val updatedOn: Date,
)

/** A person with their open balance, for the People list. */
data class DebtPersonBalance(
    val person: DebtPerson,
    /** Positive: they owe the user. Negative: the user owes them. Zero: settled. */
    val balanceMilli: Long,
    /** Earliest due date among their debts while something is still open; null otherwise. */
    val dueDate: Date?,
    /** Most recent entry date, for sorting settled people. */
    val lastActivity: Date?,
) {
    val isSettled: Boolean get() = balanceMilli == 0L
    val owesUser: Boolean get() = balanceMilli > 0L
}

/** Open debts across everyone: what the user will get back and what they owe. */
data class DebtTotals(
    val toGetMilli: Long,
    val toGiveMilli: Long,
) {
    val hasOpenDebts: Boolean get() = toGetMilli != 0L || toGiveMilli != 0L
}

private const val MILLI_SCALE = 3

/** An amount as exact thousandths (₹20.50 -> 20500), rounding half-even. */
fun Double.toMilli(): Long = BigDecimal.valueOf(this).setScale(MILLI_SCALE, RoundingMode.HALF_EVEN).unscaledValue().toLong()

/** Back to the app's Double amounts, at the edge (formatting, account balances). */
fun Long.milliToDouble(): Double = BigDecimal.valueOf(this, MILLI_SCALE).toDouble()
