package com.naveenapps.expensemanager.core.model

import kotlin.math.floor
import kotlin.math.round
import kotlin.time.Instant

/** Someone the user lends money to or borrows from. */
data class DebtPerson(
    val id: String,
    val name: String,
    val createdOn: Instant,
    val updatedOn: Instant,
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
    val date: Instant,
    /** When it should be paid back (lent / borrowed only). */
    val dueDate: Instant?,
    val note: String,
    val createdOn: Instant,
    val updatedOn: Instant,
)

/** A person with their open balance, for the People list. */
data class DebtPersonBalance(
    val person: DebtPerson,
    /** Positive: they owe the user. Negative: the user owes them. Zero: settled. */
    val balanceMilli: Long,
    /** Earliest due date among their debts while something is still open; null otherwise. */
    val dueDate: Instant?,
    /** Most recent entry date, for sorting settled people. */
    val lastActivity: Instant?,
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

/** Below 2^39 doubles are less than 0.0001 apart, so the tie test in [toMilli] is exact for any amount. */
private const val EXACT_LIMIT = 549_755_813_888.0

/** Below 2^43 doubles are less than 0.001 apart, so 3-decimal amounts are still found exactly. */
private const val EXACT_THREE_DECIMALS_LIMIT = 8_796_093_022_208.0

/**
 * An amount as exact thousandths (₹20.50 -> 20500): its shortest decimal value ×1000, rounded half
 * to even, as `BigDecimal.valueOf(this).setScale(3, HALF_EVEN)` gave before (OpenSpec change
 * make-core-model-multiplatform, design D1).
 *
 * Pure IEEE arithmetic, no `toString()` (Kotlin/Native doesn't promise the shortest text), so it
 * gives the same result on every platform. Exact for every amount below 2^39 (~550 billion) and
 * for every amount with at most 3 decimals below 2^43 (~8.8 trillion); beyond that it rounds the
 * binary value, which may differ by one milli-unit.
 */
fun Double.toMilli(): Long {
    require(isFinite()) { "Not a finite amount: $this" }
    if (this < 0) return -(-this).toMilli()
    if (this >= EXACT_LIMIT) {
        if (this < EXACT_THREE_DECIMALS_LIMIT) {
            val nearest = round(this * 1000).toLong()
            for (candidate in nearest - 2..nearest + 2) {
                if (candidate.milliToDouble() == this) return candidate
            }
        }
        return round(this * 1000).toLong()
    }
    // k/1000 <= amount < (k+1)/1000; each division is exact-input, so correctly rounded.
    var k = floor(this * 1000).toLong()
    while (k.milliToDouble() > this) k--
    while ((k + 1).milliToDouble() <= this) k++
    if (k.milliToDouble() == this) return k // a 3-decimal amount
    val tie = (10 * k + 5) / 10_000.0 // k + 0.5 thousandths
    return when {
        // Exactly x.xxx5: half to even.
        tie == this -> if (k % 2 == 0L) k else k + 1

        this > tie -> k + 1

        else -> k
    }
}

/**
 * Back to the app's Double amounts, at the edge (formatting, account balances). Equals
 * `BigDecimal.valueOf(this, 3).toDouble()` for every value below 2^53 (design D2).
 */
fun Long.milliToDouble(): Double = this / 1000.0
