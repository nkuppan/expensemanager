package com.naveenapps.expensemanager.core.domain.usecase.recurring

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.domain.usecase.transaction.AddTransactionUseCase
import com.naveenapps.expensemanager.core.model.RecurringFrequency
import com.naveenapps.expensemanager.core.model.RecurringTransaction
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.RecurringTransactionRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

class RecurringTransactionsTest {

    private val zone = ZoneId.systemDefault()

    private fun date(y: Int, m: Int, d: Int, h: Int = 9) =
        Date.from(LocalDateTime.of(y, m, d, h, 0).atZone(zone).toInstant())

    private fun Date.local() = toInstant().atZone(zone).toLocalDateTime()

    // region occurrenceDate

    @Test
    fun `monthly on the 31st clamps to short months and goes back to the 31st`() {
        val start = date(2026, 1, 31)
        val days = (1..3).map { occurrenceDate(start, RecurringFrequency.MONTHLY, it).local().toLocalDate() }
        assertThat(days.map { it.toString() }).containsExactly("2026-02-28", "2026-03-31", "2026-04-30").inOrder()
    }

    @Test
    fun `daily weekly and yearly keep the time of day`() {
        val start = date(2026, 10, 3, 18)
        assertThat(occurrenceDate(start, RecurringFrequency.DAILY, 2).local()).isEqualTo(LocalDateTime.of(2026, 10, 5, 18, 0))
        assertThat(occurrenceDate(start, RecurringFrequency.WEEKLY, 1).local()).isEqualTo(LocalDateTime.of(2026, 10, 10, 18, 0))
        assertThat(occurrenceDate(start, RecurringFrequency.YEARLY, 1).local()).isEqualTo(LocalDateTime.of(2027, 10, 3, 18, 0))
    }

    // endregion

    // region processing

    private fun rule(start: Date, frequency: RecurringFrequency = RecurringFrequency.MONTHLY) = RecurringTransaction(
        id = "rule",
        notes = "Rent",
        categoryId = "rent",
        fromAccountId = "bank",
        toAccountId = null,
        type = TransactionType.EXPENSE,
        amount = 1000.0,
        frequency = frequency,
        startDate = start,
        nextOccurrence = 1,
        nextDueDate = occurrenceDate(start, frequency, 1),
        createdOn = start,
        updatedOn = start,
    )

    @Test
    fun `missed occurrences are caught up and the rule moves past now`() = runTest {
        val rules = FakeRecurringRepository(rule(date(2026, 7, 1)))
        val transactions = FakeTransactionRepository()
        val process = ProcessRecurringTransactionsUseCase(rules, AddTransactionUseCase(transactions))

        val created = process(now = date(2026, 10, 3))

        // Aug 1, Sep 1, Oct 1 (Jul 1 was the original transaction).
        assertThat(created).isEqualTo(3)
        assertThat(transactions.added.map { it.createdOn.local().toLocalDate().toString() })
            .containsExactly("2026-08-01", "2026-09-01", "2026-10-01").inOrder()
        assertThat(transactions.added.map { it.id }.toSet()).hasSize(3)
        assertThat(transactions.added.all { it.amount.amount == 1000.0 && it.notes == "Rent" }).isTrue()
        val updated = rules.rules.value.single()
        assertThat(updated.nextOccurrence).isEqualTo(4)
        assertThat(updated.nextDueDate.local().toLocalDate().toString()).isEqualTo("2026-11-01")
    }

    @Test
    fun `running again creates nothing new`() = runTest {
        val rules = FakeRecurringRepository(rule(date(2026, 9, 1)))
        val transactions = FakeTransactionRepository()
        val process = ProcessRecurringTransactionsUseCase(rules, AddTransactionUseCase(transactions))

        process(now = date(2026, 10, 3))
        val second = process(now = date(2026, 10, 3))

        assertThat(second).isEqualTo(0)
        assertThat(transactions.added).hasSize(1)
    }

    @Test
    fun `a failed save stops that rule without skipping the occurrence`() = runTest {
        val rules = FakeRecurringRepository(rule(date(2026, 8, 1)))
        val transactions = FakeTransactionRepository(fail = true)
        val process = ProcessRecurringTransactionsUseCase(rules, AddTransactionUseCase(transactions))

        assertThat(process(now = date(2026, 10, 3))).isEqualTo(0)
        assertThat(rules.rules.value.single().nextOccurrence).isEqualTo(1)
    }

    // endregion

    private class FakeRecurringRepository(vararg initial: RecurringTransaction) : RecurringTransactionRepository {
        val rules = MutableStateFlow(initial.toList())
        override fun getAll(): Flow<List<RecurringTransaction>> = rules
        override suspend fun getDue(dueBefore: Date) = rules.value.filter { !it.nextDueDate.after(dueBefore) }
        override suspend fun add(recurring: RecurringTransaction) { rules.value = rules.value + recurring }
        override suspend fun update(recurring: RecurringTransaction) {
            rules.value = rules.value.map { if (it.id == recurring.id) recurring else it }
        }
        override suspend fun delete(id: String) { rules.value = rules.value.filterNot { it.id == id } }
    }

    private class FakeTransactionRepository(private val fail: Boolean = false) : TransactionRepository {
        val added = mutableListOf<Transaction>()
        override suspend fun addTransaction(transaction: Transaction): Resource<Boolean> {
            if (fail) return Resource.Error(Exception("fail"))
            added += transaction
            return Resource.Success(true)
        }
        override suspend fun findTransactionById(transactionId: String): Resource<Transaction> = error("unused")
        override suspend fun updateTransaction(transaction: Transaction): Resource<Boolean> = error("unused")
        override suspend fun deleteTransaction(transaction: Transaction): Resource<Boolean> = error("unused")
        override fun getAllTransaction(): Flow<List<Transaction>?> = flowOf(added)
        override fun getAllFilteredTransaction(
            accounts: List<String>,
            categories: List<String>,
            transactionType: List<Int>,
        ): Flow<List<Transaction>?> = flowOf(added)
        override fun getFilteredTransaction(
            accounts: List<String>,
            categories: List<String>,
            transactionType: List<Int>,
            startDate: Long,
            endDate: Long,
        ): Flow<List<Transaction>?> = flowOf(added)
    }
}
