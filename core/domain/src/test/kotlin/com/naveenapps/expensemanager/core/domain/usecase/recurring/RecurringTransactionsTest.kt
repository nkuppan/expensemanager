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
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RecurringTransactionsTest {

    private val zone = ZoneId.systemDefault()

    private fun date(y: Int, m: Int, d: Int, h: Int = 9) = Date.from(LocalDateTime.of(y, m, d, h, 0).atZone(zone).toInstant())

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
        val process = ProcessRecurringTransactionsUseCase(rules, transactions, AddTransactionUseCase(transactions))

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
        val process = ProcessRecurringTransactionsUseCase(rules, transactions, AddTransactionUseCase(transactions))

        process(now = date(2026, 10, 3))
        val second = process(now = date(2026, 10, 3))

        assertThat(second).isEqualTo(0)
        assertThat(transactions.added).hasSize(1)
    }

    @Test
    fun `an interrupted run never logs the same occurrence twice`() = runTest {
        val original = rule(date(2026, 7, 1))
        val rules = FakeRecurringRepository(original)
        val transactions = FakeTransactionRepository()
        val process = ProcessRecurringTransactionsUseCase(rules, transactions, AddTransactionUseCase(transactions))

        process(now = date(2026, 8, 15))
        // Simulate the process dying after the Aug 1 transaction was saved but before the
        // rule was advanced: the stored rule still points at occurrence 1.
        rules.rules.value = listOf(original)
        val created = process(now = date(2026, 10, 3))

        assertThat(created).isEqualTo(2)
        assertThat(transactions.added.map { it.createdOn.local().toLocalDate().toString() })
            .containsExactly("2026-08-01", "2026-09-01", "2026-10-01").inOrder()
        assertThat(rules.rules.value.single().nextOccurrence).isEqualTo(4)
    }

    @Test
    fun `progress is saved after every occurrence`() = runTest {
        val rules = FakeRecurringRepository(rule(date(2026, 7, 1)))
        val transactions = FakeTransactionRepository(failAfter = 1)
        val process = ProcessRecurringTransactionsUseCase(rules, transactions, AddTransactionUseCase(transactions))

        assertThat(process(now = date(2026, 10, 3))).isEqualTo(1)
        // Aug 1 was saved, Sep 1 failed: the rule now points at Sep 1, not back at Aug 1.
        assertThat(rules.rules.value.single().nextOccurrence).isEqualTo(2)
    }

    @Test
    fun `a failed save stops that rule without skipping the occurrence`() = runTest {
        val rules = FakeRecurringRepository(rule(date(2026, 8, 1)))
        val transactions = FakeTransactionRepository(fail = true)
        val process = ProcessRecurringTransactionsUseCase(rules, transactions, AddTransactionUseCase(transactions))

        assertThat(process(now = date(2026, 10, 3))).isEqualTo(0)
        assertThat(rules.rules.value.single().nextOccurrence).isEqualTo(1)
    }

    @Test
    fun `an occurrence is logged from the start of its day, not its time of day`() = runTest {
        // Rent was first added on Sep 1 at 6 pm; on Oct 1 the user opens the app in the morning.
        val rules = FakeRecurringRepository(rule(date(2026, 9, 1, h = 18)))
        val transactions = FakeTransactionRepository()
        val process = ProcessRecurringTransactionsUseCase(rules, transactions, AddTransactionUseCase(transactions))

        val created = process(now = date(2026, 10, 1, h = 9))

        assertThat(created).isEqualTo(1)
        // It keeps the rule's time of day.
        assertThat(transactions.added.single().createdOn.local()).isEqualTo(LocalDateTime.of(2026, 10, 1, 18, 0))
        assertThat(rules.rules.value.single().nextDueDate.local().toLocalDate().toString()).isEqualTo("2026-11-01")
    }

    @Test
    fun `nothing is logged the day before it is due`() = runTest {
        val rules = FakeRecurringRepository(rule(date(2026, 9, 1, h = 6)))
        val transactions = FakeTransactionRepository()
        val process = ProcessRecurringTransactionsUseCase(rules, transactions, AddTransactionUseCase(transactions))

        val created = process(now = date(2026, 9, 30, h = 23))

        assertThat(created).isEqualTo(0)
        assertThat(rules.rules.value.single().nextOccurrence).isEqualTo(1)
    }

    @Test
    fun `a template edit made while the processor runs is not overwritten`() = runTest {
        val rules = FakeRecurringRepository(rule(date(2026, 9, 1)))
        val transactions = FakeTransactionRepository()
        val process = ProcessRecurringTransactionsUseCase(rules, transactions, AddTransactionUseCase(transactions))
        // The user saves a new amount after the processor has read the due rules.
        rules.afterGetDue = {
            rules.rules.value = rules.rules.value.map { it.copy(amount = 1500.0, notes = "New rent") }
        }

        process(now = date(2026, 10, 3))

        val stored = rules.rules.value.single()
        assertThat(stored.amount).isEqualTo(1500.0)
        assertThat(stored.notes).isEqualTo("New rent")
        // Oct 1 was still logged and the rule moved on to Nov 1.
        assertThat(stored.nextOccurrence).isEqualTo(2)
    }

    // endregion

    // region editing

    private fun updateUseCase(rules: FakeRecurringRepository, transactions: FakeTransactionRepository = FakeTransactionRepository()) = UpdateRecurringTransactionUseCase(rules, ProcessRecurringTransactionsUseCase(rules, transactions, AddTransactionUseCase(transactions)))

    @Test
    fun `a new rule dated in the past fills in the missed months straight away`() = runTest {
        // Rent entered today (Oct 3) with a start date of Jul 1; the Jul 1 transaction itself
        // was just saved by the create screen.
        val rules = FakeRecurringRepository()
        val transactions = FakeTransactionRepository()
        val create = CreateRecurringTransactionUseCase(rules, ProcessRecurringTransactionsUseCase(rules, transactions, AddTransactionUseCase(transactions)))
        val first = Transaction(
            id = "first",
            notes = "Rent",
            categoryId = "rent",
            fromAccountId = "bank",
            toAccountId = null,
            type = TransactionType.EXPENSE,
            amount = com.naveenapps.expensemanager.core.model.Amount(1000.0),
            imagePath = "",
            createdOn = date(2026, 7, 1),
            updatedOn = date(2026, 7, 1),
        )

        create(first, RecurringFrequency.MONTHLY, now = date(2026, 10, 3))

        assertThat(transactions.added.map { it.createdOn.local().toLocalDate().toString() })
            .containsExactly("2026-08-01", "2026-09-01", "2026-10-01").inOrder()
        assertThat(rules.rules.value.single().nextDueDate.local().toLocalDate().toString()).isEqualTo("2026-11-01")
    }

    @Test
    fun `moving the next date to today logs it straight away`() = runTest {
        val original = rule(date(2026, 9, 1)).copy(nextOccurrence = 1, nextDueDate = date(2026, 10, 1))
        val rules = FakeRecurringRepository(original)
        val transactions = FakeTransactionRepository()

        updateUseCase(rules, transactions)("rule", changes(original, nextDueDate = date(2026, 10, 3, h = 8)), now = date(2026, 10, 3, h = 9))

        assertThat(transactions.added.map { it.createdOn.local().toLocalDate().toString() }).containsExactly("2026-10-03")
    }

    private fun changes(
        base: RecurringTransaction,
        amount: Double = base.amount,
        notes: String = base.notes,
        frequency: RecurringFrequency = base.frequency,
        nextDueDate: Date = base.nextDueDate,
    ) = RecurringTransactionChanges(
        amount = amount,
        notes = notes,
        categoryId = base.categoryId,
        fromAccountId = base.fromAccountId,
        toAccountId = base.toAccountId,
        frequency = frequency,
        nextDueDate = nextDueDate,
    )

    @Test
    fun `template edit updates the rule in place and keeps its schedule`() = runTest {
        val original = rule(date(2026, 9, 1)).copy(nextOccurrence = 2, nextDueDate = date(2026, 11, 1))
        val rules = FakeRecurringRepository(original)

        val result = updateUseCase(rules)(
            "rule",
            changes(original, amount = 1200.0, notes = "Rent + parking"),
            now = date(2026, 10, 3),
        )

        assertThat(result).isInstanceOf(Resource.Success::class.java)
        val stored = rules.rules.value.single()
        assertThat(stored.id).isEqualTo("rule")
        assertThat(stored.amount).isEqualTo(1200.0)
        assertThat(stored.notes).isEqualTo("Rent + parking")
        assertThat(stored.nextOccurrence).isEqualTo(2)
        assertThat(stored.startDate).isEqualTo(original.startDate)
    }

    @Test
    fun `edited template is used for the next occurrence but past ones are untouched`() = runTest {
        val rules = FakeRecurringRepository(rule(date(2026, 8, 1)))
        val transactions = FakeTransactionRepository()
        val process = ProcessRecurringTransactionsUseCase(rules, transactions, AddTransactionUseCase(transactions))
        process(now = date(2026, 9, 15)) // logs Sep 1 at 1000

        updateUseCase(rules, transactions)(
            "rule",
            changes(rules.rules.value.single(), amount = 1200.0),
            now = date(2026, 9, 15),
        )
        process(now = date(2026, 10, 3)) // logs Oct 1 at the new amount

        assertThat(transactions.added.map { it.amount.amount }).containsExactly(1000.0, 1200.0).inOrder()
    }

    @Test
    fun `schedule change starts a new series that never reuses an occurrence id`() = runTest {
        val rules = FakeRecurringRepository(rule(date(2026, 8, 1)))
        val transactions = FakeTransactionRepository()
        val process = ProcessRecurringTransactionsUseCase(rules, transactions, AddTransactionUseCase(transactions))
        process(now = date(2026, 9, 15)) // logs Sep 1 as occurrence 1

        val result = updateUseCase(rules, transactions)(
            "rule",
            changes(rules.rules.value.single(), frequency = RecurringFrequency.WEEKLY, nextDueDate = date(2026, 9, 20)),
            now = date(2026, 9, 15),
        )
        process(now = date(2026, 10, 1))

        val newRule = (result as Resource.Success).data
        assertThat(newRule.id).isNotEqualTo("rule")
        assertThat(rules.rules.value.map { it.id }).containsExactly(newRule.id)
        // Sep 1 (old series), then Sep 20 and Sep 27 from the new weekly series.
        assertThat(transactions.added.map { it.createdOn.local().toLocalDate().toString() })
            .containsExactly("2026-09-01", "2026-09-20", "2026-09-27").inOrder()
        assertThat(transactions.added.map { it.id }.toSet()).hasSize(3)
    }

    @Test
    fun `schedule change to a past date is rejected and nothing changes`() = runTest {
        val original = rule(date(2026, 9, 1))
        val rules = FakeRecurringRepository(original)

        val result = updateUseCase(rules)(
            "rule",
            changes(original, nextDueDate = date(2026, 9, 10)),
            now = date(2026, 10, 3),
        )

        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat(rules.rules.value.single()).isEqualTo(original)
    }

    @Test
    fun `editing a deleted rule fails`() = runTest {
        val rules = FakeRecurringRepository()

        val result = updateUseCase(rules)("gone", changes(rule(date(2026, 9, 1))))

        assertThat(result).isInstanceOf(Resource.Error::class.java)
    }

    @Test
    fun `rule id is recovered from an occurrence id`() {
        val ruleId = "3f2a9c1e-7b4d-4e8a-9a51-0c6d2b7e8f10"
        val occurrenceId = ProcessRecurringTransactionsUseCase.occurrenceTransactionId(ruleId, 12)

        assertThat(ProcessRecurringTransactionsUseCase.ruleIdFromOccurrenceId(occurrenceId)).isEqualTo(ruleId)
        assertThat(ProcessRecurringTransactionsUseCase.ruleIdFromOccurrenceId("plain-transaction-id")).isNull()
        assertThat(ProcessRecurringTransactionsUseCase.ruleIdFromOccurrenceId("recurring-abc-notanumber")).isNull()
    }

    // endregion

    private class FakeRecurringRepository(vararg initial: RecurringTransaction) : RecurringTransactionRepository {
        val rules = MutableStateFlow(initial.toList())

        /** Runs right after getDue returns, to simulate something changing mid-run. */
        var afterGetDue: (() -> Unit)? = null

        override fun getAll(): Flow<List<RecurringTransaction>> = rules
        override suspend fun getDue(dueBefore: Date) = rules.value.filter { !it.nextDueDate.after(dueBefore) }
            .also { afterGetDue?.invoke() }
        override suspend fun findById(id: String) = rules.value.firstOrNull { it.id == id }
        override suspend fun advance(id: String, nextOccurrence: Int, nextDueDate: Date, updatedOn: Date) {
            rules.value = rules.value.map {
                if (it.id == id) it.copy(nextOccurrence = nextOccurrence, nextDueDate = nextDueDate, updatedOn = updatedOn) else it
            }
        }
        override suspend fun replace(oldId: String, replacement: RecurringTransaction) {
            rules.value = rules.value.filterNot { it.id == oldId } + replacement
        }
        override suspend fun add(recurring: RecurringTransaction) {
            rules.value = rules.value + recurring
        }
        override suspend fun update(recurring: RecurringTransaction) {
            rules.value = rules.value.map { if (it.id == recurring.id) recurring else it }
        }
        override suspend fun delete(id: String) {
            rules.value = rules.value.filterNot { it.id == id }
        }
    }

    private class FakeTransactionRepository(
        private val fail: Boolean = false,
        private val failAfter: Int = Int.MAX_VALUE,
    ) : TransactionRepository {
        val added = mutableListOf<Transaction>()
        override suspend fun addTransaction(transaction: Transaction): Resource<Boolean> {
            if (fail || added.size >= failAfter) return Resource.Error(Exception("fail"))
            added += transaction
            return Resource.Success(true)
        }
        override suspend fun findTransactionById(transactionId: String): Resource<Transaction> = added.firstOrNull { it.id == transactionId }?.let { Resource.Success(it) }
            ?: Resource.Error(NoSuchElementException(transactionId))
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
