package com.naveenapps.expensemanager.core.domain.usecase.debt

import com.naveenapps.expensemanager.core.model.DebtEntry
import com.naveenapps.expensemanager.core.model.DebtPerson
import com.naveenapps.expensemanager.core.model.DebtPersonBalance
import com.naveenapps.expensemanager.core.model.DebtTotals
import kotlin.time.Instant

/** The ledger's arithmetic, kept pure so it's easy to test. All amounts are exact milli-units. */
object DebtLedger {

    /** What the person owes the user (positive) or the user owes them (negative). */
    fun balanceOf(entries: List<DebtEntry>): Long = entries.fold(0L) { total, entry ->
        addExact(total, entry.kind.balanceSign * entry.amountMilli)
    }

    /**
     * The earliest due date of a debt in the direction still open: while they owe the user, the
     * money lent; while the user owes them, the money borrowed. Null once settled.
     */
    fun dueDateOf(entries: List<DebtEntry>, balanceMilli: Long): Instant? {
        if (balanceMilli == 0L) return null
        val openSign = if (balanceMilli > 0) 1 else -1
        return entries
            .filter { it.kind.isNewDebt && it.kind.balanceSign == openSign }
            .mapNotNull { it.dueDate }
            .minOrNull()
    }

    /**
     * Everyone with their balance: open debts first (soonest due, then largest), then settled
     * people (most recent activity first).
     */
    fun balances(people: List<DebtPerson>, entries: List<DebtEntry>): List<DebtPersonBalance> {
        val byPerson = entries.groupBy { it.personId }
        val all = people.map { person ->
            val own = byPerson[person.id].orEmpty()
            val balance = balanceOf(own)
            DebtPersonBalance(
                person = person,
                balanceMilli = balance,
                dueDate = dueDateOf(own, balance),
                lastActivity = own.maxOfOrNull { it.date },
            )
        }
        val (open, settled) = all.partition { !it.isSettled }
        return open.sortedWith(
            compareBy<DebtPersonBalance, Long?>(nullsLast()) { it.dueDate?.toEpochMilliseconds() }
                .thenByDescending { kotlin.math.abs(it.balanceMilli) },
        ) + settled.sortedByDescending { it.lastActivity?.toEpochMilliseconds() ?: it.person.updatedOn.toEpochMilliseconds() }
    }

    fun totals(balances: List<DebtPersonBalance>): DebtTotals = DebtTotals(
        toGetMilli = balances.filter { it.balanceMilli > 0 }.sumOf { it.balanceMilli },
        toGiveMilli = balances.filter { it.balanceMilli < 0 }.sumOf { -it.balanceMilli },
    )

    /**
     * `a + b`, throwing [ArithmeticException] on overflow instead of wrapping round: the JDK's
     * `Math.addExact` algorithm and message, which common code can't call.
     */
    private fun addExact(a: Long, b: Long): Long {
        val result = a + b
        if (((a xor result) and (b xor result)) < 0) throw ArithmeticException("long overflow")
        return result
    }
}
