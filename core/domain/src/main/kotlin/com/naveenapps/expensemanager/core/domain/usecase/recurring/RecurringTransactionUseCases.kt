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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Date
import java.util.UUID

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
 * background job): the lock stops two runs creating the same occurrence twice.
 */
class ProcessRecurringTransactionsUseCase(
    private val repository: RecurringTransactionRepository,
    private val addTransactionUseCase: AddTransactionUseCase,
) {
    /** Returns how many transactions were created. */
    suspend operator fun invoke(now: Date = Date()): Int = lock.withLock {
        var created = 0
        repository.getDue(now).forEach { rule ->
            var index = rule.nextOccurrence
            var due = rule.nextDueDate
            var createdForRule = 0
            while (!due.after(now) && createdForRule < MAX_CATCH_UP_PER_RUN) {
                val result = addTransactionUseCase.invoke(rule.toTransaction(due, now))
                if (result !is Resource.Success) break
                createdForRule++
                index++
                due = occurrenceDate(rule.startDate, rule.frequency, index)
            }
            if (createdForRule > 0) {
                repository.update(rule.copy(nextOccurrence = index, nextDueDate = due, updatedOn = now))
                created += createdForRule
            }
        }
        created
    }

    private fun RecurringTransaction.toTransaction(due: Date, now: Date) = Transaction(
        id = UUID.randomUUID().toString(),
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
