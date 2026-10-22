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
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Makes [transaction] (already saved, occurrence 0) repeat every [frequency]. */
class CreateRecurringTransactionUseCase(
    private val repository: RecurringTransactionRepository,
) {
    suspend operator fun invoke(transaction: Transaction, frequency: RecurringFrequency) {
        val now = Date()
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
    }
}

/**
 * Logs every occurrence that has come due, including ones missed while the app wasn't opened,
 * through the normal add path so account balances stay right. Safe to call often (app start,
 * background job) and safe to interrupt:
 * - each occurrence gets a deterministic id (rule id + occurrence index) and is skipped if it
 *   already exists, so a run killed after saving a transaction but before advancing the rule
 *   never creates it twice;
 * - the rule is advanced after every occurrence, so an interrupted catch-up resumes where it
 *   stopped instead of starting over;
 * - the lock stops two runs in the same process from racing.
 */
class ProcessRecurringTransactionsUseCase(
    private val repository: RecurringTransactionRepository,
    private val transactionRepository: TransactionRepository,
    private val addTransactionUseCase: AddTransactionUseCase,
) {
    /** Returns how many transactions were created. */
    suspend operator fun invoke(now: Date = Date()): Int = lock.withLock {
        var created = 0
        repository.getDue(now).forEach { rule ->
            var current = rule
            var processedForRule = 0
            while (!current.nextDueDate.after(now) && processedForRule < MAX_CATCH_UP_PER_RUN) {
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
                repository.update(current)
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

    companion object {
        // A year of daily entries; anything older is caught up on the next run.
        private const val MAX_CATCH_UP_PER_RUN = 366
        private val lock = Mutex()

        /** Stable id of the [index]-th occurrence of a rule, used to detect it was already logged. */
        fun occurrenceTransactionId(ruleId: String, index: Int): String = "recurring-$ruleId-$index"
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
