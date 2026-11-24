package com.naveenapps.expensemanager.core.domain.usecase.debt

import com.naveenapps.expensemanager.core.model.DebtEntry
import com.naveenapps.expensemanager.core.model.DebtEntryKind
import com.naveenapps.expensemanager.core.model.DebtPerson
import com.naveenapps.expensemanager.core.model.milliToDouble
import com.naveenapps.expensemanager.core.model.toMilli
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class DebtLedgerTest {

    private val ravi = DebtPerson("ravi", "Ravi", Instant.fromEpochMilliseconds(0), Instant.fromEpochMilliseconds(0))
    private val priya = DebtPerson("priya", "Priya", Instant.fromEpochMilliseconds(0), Instant.fromEpochMilliseconds(0))
    private val mom = DebtPerson("mom", "Mom", Instant.fromEpochMilliseconds(0), Instant.fromEpochMilliseconds(0))

    private fun entry(
        person: DebtPerson,
        kind: DebtEntryKind,
        amount: Double,
        date: Long = 1_000,
        due: Long? = null,
    ) = DebtEntry(
        id = "${person.id}-$kind-$amount-$date",
        personId = person.id,
        kind = kind,
        amountMilli = amount.toMilli(),
        accountId = null,
        date = Instant.fromEpochMilliseconds(date),
        dueDate = due?.let { Instant.fromEpochMilliseconds(it) },
        note = "",
        createdOn = Instant.fromEpochMilliseconds(date),
        updatedOn = Instant.fromEpochMilliseconds(date),
    )

    @Test
    fun lendingAndPartialRepaymentLeaveTheRest() {
        val entries = listOf(
            entry(ravi, DebtEntryKind.LENT, 2000.0),
            entry(ravi, DebtEntryKind.RECEIVED, 500.0),
        )

        assertEquals(1500.0.toMilli(), DebtLedger.balanceOf(entries))
    }

    @Test
    fun borrowingMakesTheBalanceNegativeAndPayingBackClearsIt() {
        val borrowed = listOf(entry(priya, DebtEntryKind.BORROWED, 800.0))
        assertEquals(-800.0.toMilli(), DebtLedger.balanceOf(borrowed))

        val settled = borrowed + entry(priya, DebtEntryKind.PAID, 800.0)
        assertEquals(0L, DebtLedger.balanceOf(settled))
    }

    @Test
    fun decimalsAreExact() {
        // 0.1 + 0.2 as doubles isn't 0.3; as milli-units it is.
        val entries = listOf(
            entry(ravi, DebtEntryKind.LENT, 0.1),
            entry(ravi, DebtEntryKind.LENT, 0.2),
            entry(ravi, DebtEntryKind.RECEIVED, 0.3),
        )

        assertEquals(0L, DebtLedger.balanceOf(entries))
        assertEquals(20_500L, 20.5.toMilli())
        assertEquals(20.5, 20_500L.milliToDouble())
    }

    @Test
    fun dueDateIsTheEarliestOpenDebtInTheOpenDirection() {
        val entries = listOf(
            entry(ravi, DebtEntryKind.LENT, 100.0, due = 5_000),
            entry(ravi, DebtEntryKind.LENT, 100.0, due = 3_000),
            entry(ravi, DebtEntryKind.BORROWED, 50.0, due = 1_000),
        )

        assertEquals(Instant.fromEpochMilliseconds(3_000), DebtLedger.dueDateOf(entries, DebtLedger.balanceOf(entries)))
        assertNull(DebtLedger.dueDateOf(entries, 0L))
    }

    @Test
    fun openDebtsComeFirstSoonestDueThenLargestAndSettledLast() {
        val entries = listOf(
            entry(ravi, DebtEntryKind.LENT, 1500.0, due = 9_000),
            entry(priya, DebtEntryKind.BORROWED, 800.0, due = 2_000),
            entry(mom, DebtEntryKind.LENT, 300.0, date = 7_000),
            entry(mom, DebtEntryKind.RECEIVED, 300.0, date = 8_000),
        )

        val balances = DebtLedger.balances(listOf(mom, ravi, priya), entries)

        assertEquals(listOf("priya", "ravi", "mom"), balances.map { it.person.id })
        assertTrue(balances.last().isSettled)
    }

    @Test
    fun totalsSplitWhatTheUserGetsAndGives() {
        val balances = DebtLedger.balances(
            listOf(ravi, priya),
            listOf(entry(ravi, DebtEntryKind.LENT, 1500.0), entry(priya, DebtEntryKind.BORROWED, 800.0)),
        )

        val totals = DebtLedger.totals(balances)

        assertEquals(1500.0.toMilli(), totals.toGetMilli)
        assertEquals(800.0.toMilli(), totals.toGiveMilli)
        assertTrue(totals.hasOpenDebts)
    }
}
