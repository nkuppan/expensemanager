package com.naveenapps.expensemanager.core.domain.usecase.recurring

import com.naveenapps.expensemanager.core.domain.usecase.transaction.AddTransactionUseCase
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.RecurringFrequency
import com.naveenapps.expensemanager.core.model.RecurringTransaction
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import com.naveenapps.expensemanager.core.repository.RecurringTransactionRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import java.time.ZoneId
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Serialises everything that reads-then-writes a rule (the background processor and user edits)
 * within this process.
 */
internal val recurringRulesLock = Mutex()

/**
 * Makes [transaction] (already saved, occurrence 0) repeat every [frequency], then logs any
 * occurrences that are already due. Without that, a transaction dated in the past (rent "since
 * August") would show only its first month until the next app start or background run, which
 * can be days away because Android keeps the app's process alive.
 */
class CreateRecurringTransactionUseCase(
    private val repository: RecurringTransactionRepository,
    private val processRecurringTransactions: ProcessRecurringTransactionsUseCase,
) {
    suspend operator fun invoke(transaction: Transaction, frequency: RecurringFrequency, now: Date = Date()) {
        repository.add(
            RecurringTransaction(
                id = UUID.randomUUID().toString(),
                notes = transaction.notes,
                categoryId = transaction.categoryId,
                fromAccountId = transaction.fromAccountId,
                toAccountId = transaction.toAccountId,
                type = transaction.type,
                amount = transaction.amount.amount,
                frequency = frequency,
                startDate = transaction.createdOn,
                nextOccurrence = 1,
                nextDueDate = occurrenceDate(transaction.createdOn, frequency, 1),
                createdOn = now,
                updatedOn = now,
            ),
        )
        processRecurringTransactions(now)
    }
}

/**
 * Logs every occurrence that has come due, including ones missed while the app wasn't opened,
 * through the normal add path so account balances stay right.
 *
 * An occurrence is due from the start of its day, not the exact minute: a rule created at 6 pm
 * repeats "on the 1st", so opening the app on the morning of the 1st logs it then. (It keeps its
 * time of day, so the transaction's date and time are the same as before.) Nothing is ever
 * logged a day early.
 *
 * Safe to call often (app start, background job) and safe to interrupt:
 * - each occurrence gets a deterministic id (rule id + occurrence index) and is skipped if it
 *   already exists, so a run killed after saving a transaction but before advancing the rule
 *   never creates it twice;
 * - the rule is advanced after every occurrence, so an interrupted catch-up resumes where it
 *   stopped instead of starting over;
 * - the lock stops two runs in the same process from racing, and also serialises runs with
 *   user edits ([UpdateRecurringTransactionUseCase]);
 * - only the schedule progress is written back ([RecurringTransactionRepository.advance]),
 *   never the whole row, so a template edit can't be overwritten with a stale copy.
 */
class ProcessRecurringTransactionsUseCase(
    private val repository: RecurringTransactionRepository,
    private val transactionRepository: TransactionRepository,
    private val addTransactionUseCase: AddTransactionUseCase,
) {
    /** Returns how many transactions were created. */
    suspend operator fun invoke(now: Date = Date(), zone: ZoneId = ZoneId.systemDefault()): Int = recurringRulesLock.withLock {
        val dueBy = endOfDay(now, zone)
        var created = 0
        repository.getDue(dueBy).forEach { rule ->
            var current = rule
            var processedForRule = 0
            while (!current.nextDueDate.after(dueBy) && processedForRule < MAX_CATCH_UP_PER_RUN) {
                val transaction = current.toTransaction(current.nextOccurrence, current.nextDueDate, now)
                val alreadyCreated =
                    transactionRepository.findTransactionById(transaction.id) is Resource.Success
                if (!alreadyCreated) {
                    val result = addTransactionUseCase.invoke(transaction)
                    if (result !is Resource.Success) break
                    created++
                }
                processedForRule++
                val nextIndex = current.nextOccurrence + 1
                current = current.copy(
                    nextOccurrence = nextIndex,
                    nextDueDate = occurrenceDate(rule.startDate, rule.frequency, nextIndex),
                    updatedOn = now,
                )
                repository.advance(current.id, current.nextOccurrence, current.nextDueDate, now)
            }
        }
        created
    }

    private fun RecurringTransaction.toTransaction(index: Int, due: Date, now: Date) = Transaction(
        id = occurrenceTransactionId(id, index),
        notes = notes,
        categoryId = categoryId,
        fromAccountId = fromAccountId,
        toAccountId = toAccountId,
        type = type,
        amount = Amount(amount),
        imagePath = "",
        createdOn = due,
        updatedOn = now,
    )

    /** The last millisecond of [now]'s calendar day in [zone]. */
    private fun endOfDay(now: Date, zone: ZoneId): Date {
        val nextDay = now.toInstant().atZone(zone).toLocalDate().plusDays(1).atStartOfDay(zone)
        return Date(nextDay.toInstant().toEpochMilli() - 1)
    }

    companion object {
        // A year of daily entries; anything older is caught up on the next run.
        private const val MAX_CATCH_UP_PER_RUN = 366

        /** Stable id of the [index]-th occurrence of a rule, used to detect it was already logged. */
        fun occurrenceTransactionId(ruleId: String, index: Int): String = "$OCCURRENCE_PREFIX$ruleId-$index"

        /**
         * The rule that logged [transactionId], or null if it wasn't logged by a rule (the
         * original transaction a rule was created from has an ordinary id).
         */
        fun ruleIdFromOccurrenceId(transactionId: String): String? {
            if (!transactionId.startsWith(OCCURRENCE_PREFIX)) return null
            val rest = transactionId.removePrefix(OCCURRENCE_PREFIX)
            val separator = rest.lastIndexOf('-')
            if (separator <= 0 || rest.substring(separator + 1).toIntOrNull() == null) return null
            return rest.substring(0, separator)
        }

        private const val OCCURRENCE_PREFIX = "recurring-"
    }
}

/** All rules with their category and accounts filled in, soonest due first. */
class GetRecurringTransactionsUseCase(
    private val repository: RecurringTransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
) {
    operator fun invoke(): Flow<List<RecurringTransaction>> = combine(
        repository.getAll(),
        accountRepository.getAccounts(),
        categoryRepository.getCategories(),
    ) { rules, accounts, categories ->
        val accountsById = accounts.associateBy { it.id }
        val categoriesById = categories.associateBy { it.id }
        rules.map { rule ->
            rule.copy(
                category = categoriesById[rule.categoryId],
                fromAccount = accountsById[rule.fromAccountId],
                toAccount = rule.toAccountId?.let { accountsById[it] },
            )
        }
    }
}

/** Stops a rule. Transactions it already created are kept. */
class DeleteRecurringTransactionUseCase(
    private val repository: RecurringTransactionRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}

/** A single rule (display fields not filled in), or null if it was deleted. */
class FindRecurringTransactionByIdUseCase(
    private val repository: RecurringTransactionRepository,
) {
    suspend operator fun invoke(id: String): RecurringTransaction? = repository.findById(id)
}

/** What the user can change on a rule. The transaction type is fixed once a rule exists. */
data class RecurringTransactionChanges(
    val amount: Double,
    val notes: String,
    val categoryId: String,
    val fromAccountId: String,
    val toAccountId: String?,
    val frequency: RecurringFrequency,
    val nextDueDate: Date,
)

/**
 * Saves edits to a rule. A rule is the parent of its occurrences: edits only shape occurrences
 * that haven't been created yet, and transactions already logged are never touched.
 *
 * - Template-only edits (amount, notes, category, accounts) update the rule in place.
 * - Schedule edits (frequency or next date) end the rule and start a new one, with a new id,
 *   anchored on the new next date. Re-anchoring the same rule would restart its occurrence
 *   numbering, and occurrence ids are `recurring-<ruleId>-<n>`, so the processor would mistake
 *   new occurrences for ones it already logged and silently skip them.
 */
class UpdateRecurringTransactionUseCase(
    private val repository: RecurringTransactionRepository,
    private val processRecurringTransactions: ProcessRecurringTransactionsUseCase,
) {
    suspend operator fun invoke(
        id: String,
        changes: RecurringTransactionChanges,
        now: Date = Date(),
    ): Resource<RecurringTransaction> = recurringRulesLock.withLock {
        val current = repository.findById(id)
            ?: return@withLock Resource.Error(NoSuchElementException("Recurring transaction $id not found"))

        val withTemplate = current.copy(
            amount = changes.amount,
            notes = changes.notes,
            categoryId = changes.categoryId,
            fromAccountId = changes.fromAccountId,
            toAccountId = changes.toAccountId,
            updatedOn = now,
        )

        val scheduleChanged = changes.frequency != current.frequency ||
            changes.nextDueDate.time != current.nextDueDate.time

        if (!scheduleChanged) {
            repository.update(withTemplate)
            return@withLock Resource.Success(withTemplate)
        }

        if (changes.nextDueDate.before(startOfDay(now))) {
            return@withLock Resource.Error(IllegalArgumentException("Next date can't be in the past"))
        }

        val replacement = withTemplate.copy(
            id = UUID.randomUUID().toString(),
            frequency = changes.frequency,
            startDate = changes.nextDueDate,
            // Occurrence 0 of the new rule is the chosen next date itself.
            nextOccurrence = 0,
            nextDueDate = changes.nextDueDate,
            createdOn = now,
        )
        repository.replace(oldId = id, replacement = replacement)
        Resource.Success(replacement)
    }.also { result ->
        // Outside the lock (processing takes it too): log anything already due under the new
        // schedule, e.g. a next date of today, instead of waiting for the next app start.
        if (result is Resource.Success) processRecurringTransactions(now)
    }

    private fun startOfDay(date: Date): Date {
        val zone = java.time.ZoneId.systemDefault()
        val start = date.toInstant().atZone(zone).toLocalDate().atStartOfDay(zone)
        return Date.from(start.toInstant())
    }
}
