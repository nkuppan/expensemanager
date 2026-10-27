package com.naveenapps.expensemanager.core.domain.usecase.recurring

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.domain.usecase.transaction.FilterValue
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.RecurringFrequency
import com.naveenapps.expensemanager.core.model.RecurringTransaction
import com.naveenapps.expensemanager.core.model.TransactionType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import org.junit.Test

class UpcomingRecurringTransactionsTest {

    private val zone = ZoneId.systemDefault()
    private val now = millis(2026, 10, 3, 9)

    private fun millis(y: Int, m: Int, d: Int, h: Int = 0) = LocalDateTime.of(y, m, d, h, 0).atZone(zone).toInstant().toEpochMilli()

    private fun month(y: Int, m: Int): List<Long> {
        val first = LocalDate.of(y, m, 1)
        return listOf(first.atStartOfDay(zone).toInstant().toEpochMilli(), first.plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1)
    }

    private fun filter(range: List<Long>, type: DateRangeType = DateRangeType.CUSTOM) = FilterValue(
        dateRangeType = type,
        filterRange = range,
        accounts = listOf("bank"),
        categories = listOf("rent", "food"),
        transactionTypes = TransactionType.entries.map { it.ordinal },
    )

    private fun rule(
        id: String = "rent",
        frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
        start: Long = millis(2026, 9, 1, 18),
        nextOccurrence: Int = 2,
        categoryId: String = "rent",
        accountId: String = "bank",
        type: TransactionType = TransactionType.EXPENSE,
    ): RecurringTransaction {
        val startDate = Date(start)
        return RecurringTransaction(
            id = id,
            notes = "Rent",
            categoryId = categoryId,
            fromAccountId = accountId,
            toAccountId = null,
            type = type,
            amount = 1000.0,
            frequency = frequency,
            startDate = startDate,
            nextOccurrence = nextOccurrence,
            nextDueDate = occurrenceDate(startDate, frequency, nextOccurrence, zone),
            createdOn = startDate,
            updatedOn = startDate,
        )
    }

    private fun days(list: List<com.naveenapps.expensemanager.core.model.Transaction>) = list.map { it.createdOn.toInstant().atZone(zone).toLocalDate().toString() }

    @Test
    fun `a future month shows its planned occurrence`() {
        // Sep 1 (the original, occurrence 0) and Oct 1 (occurrence 1) are logged; viewing
        // November on Oct 3.
        val upcoming = upcomingOccurrences(listOf(rule()), filter(month(2026, 11)), now, zone)

        assertThat(days(upcoming)).containsExactly("2026-11-01")
        assertThat(upcoming.single().id).isEqualTo(ProcessRecurringTransactionsUseCase.occurrenceTransactionId("rent", 2))
        assertThat(upcoming.single().amount.amount).isEqualTo(1000.0)
    }

    @Test
    fun `the current month lists only what is still to come, never today or earlier`() {
        val weekly = rule(id = "groceries", frequency = RecurringFrequency.WEEKLY, start = millis(2026, 9, 5, 10), nextOccurrence = 4, categoryId = "food")
        // Occurrences: Sep 26 (index 3, logged), Oct 3 (index 4, due today: the processor's job), Oct 10, 17, 24, 31.
        val upcoming = upcomingOccurrences(listOf(weekly), filter(month(2026, 10)), now, zone)

        assertThat(days(upcoming)).containsExactly("2026-10-31", "2026-10-24", "2026-10-17", "2026-10-10").inOrder()
    }

    @Test
    fun `a past month shows nothing planned`() {
        assertThat(upcomingOccurrences(listOf(rule()), filter(month(2026, 9)), now, zone)).isEmpty()
    }

    @Test
    fun `filters for accounts, categories and types apply`() {
        val rules = listOf(
            rule(id = "other-account", accountId = "cash"),
            rule(id = "other-category", categoryId = "travel"),
            rule(id = "income", type = TransactionType.INCOME),
        )
        val expensesOnly = filter(month(2026, 11)).copy(transactionTypes = listOf(TransactionType.EXPENSE.ordinal))

        assertThat(upcomingOccurrences(rules, expensesOnly, now, zone)).isEmpty()
    }

    @Test
    fun `the all range looks one year ahead`() {
        val upcoming = upcomingOccurrences(listOf(rule()), filter(listOf(0L, 0L), DateRangeType.ALL), now, zone)

        // Nov 1 2026 through Oct 1 2027.
        assertThat(upcoming).hasSize(12)
        assertThat(days(upcoming).first()).isEqualTo("2027-10-01")
        assertThat(days(upcoming).last()).isEqualTo("2026-11-01")
    }

    @Test
    fun `a rule waiting to be processed is not shown twice`() {
        // The app hasn't run since Oct 1 was due: next occurrence is still Oct 1 (index 1).
        val pending = rule(nextOccurrence = 1)
        val upcoming = upcomingOccurrences(listOf(pending), filter(month(2026, 10)), now, zone)

        assertThat(upcoming).isEmpty()
    }
}
