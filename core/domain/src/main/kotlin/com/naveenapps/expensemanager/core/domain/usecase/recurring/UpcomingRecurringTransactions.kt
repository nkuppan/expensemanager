package com.naveenapps.expensemanager.core.domain.usecase.recurring

import com.naveenapps.expensemanager.core.domain.usecase.transaction.FilterValue
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.RecurringTransaction
import com.naveenapps.expensemanager.core.model.Transaction
import java.time.ZoneId
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Recurring transactions that haven't happened yet, inside the selected date range and filters,
 * so browsing a future month shows what's planned instead of an empty list.
 *
 * These are previews only: nothing is saved and balances don't change. Each one carries the id
 * its real transaction will get when it's logged (see
 * [ProcessRecurringTransactionsUseCase.occurrenceTransactionId]), so it can never show twice and
 * its rule can be found from it.
 */
class GetUpcomingRecurringTransactionsUseCase(
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val getRecurringTransactionsUseCase: GetRecurringTransactionsUseCase,
) {
    operator fun invoke(
        now: () -> Long = { System.currentTimeMillis() },
        zone: ZoneId = ZoneId.systemDefault(),
    ): Flow<List<Transaction>> = combine(
        getTransactionWithFilterUseCase.filterValues(),
        getRecurringTransactionsUseCase.invoke(),
    ) { filter, rules ->
        upcomingOccurrences(rules, filter, now(), zone)
    }
}

/**
 * Occurrences of [rules] after today and within [filter]'s date range (or the next year for the
 * "All" range), newest first, matching its accounts, categories and types. Anything due today or
 * earlier is the processor's job (it logs it as a real transaction), so it's never listed here.
 */
internal fun upcomingOccurrences(
    rules: List<RecurringTransaction>,
    filter: FilterValue,
    nowMillis: Long,
    zone: ZoneId = ZoneId.systemDefault(),
): List<Transaction> {
    val today = java.time.Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
    val tomorrowStart = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val (from, to) = if (filter.dateRangeType == DateRangeType.ALL || filter.filterRange.size < 2) {
        tomorrowStart to today.plusYears(1).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
    } else {
        maxOf(filter.filterRange[0], tomorrowStart) to filter.filterRange[1]
    }
    if (to < from) return emptyList()

    val upcoming = ArrayList<Transaction>()
    for (rule in rules) {
        if (rule.fromAccountId !in filter.accounts) continue
        if (rule.categoryId !in filter.categories) continue
        if (rule.type.ordinal !in filter.transactionTypes) continue
        var index = rule.nextOccurrence
        var due = rule.nextDueDate
        var listed = 0
        while (due.time <= to && listed < MAX_PER_RULE) {
            if (due.time >= from) {
                upcoming += rule.preview(index, due)
                listed++
            }
            index++
            due = occurrenceDate(rule.startDate, rule.frequency, index, zone)
        }
    }
    return upcoming.sortedByDescending { it.createdOn }
}

private fun RecurringTransaction.preview(index: Int, due: Date): Transaction {
    val transaction = Transaction(
        id = ProcessRecurringTransactionsUseCase.occurrenceTransactionId(id, index),
        notes = notes,
        categoryId = categoryId,
        fromAccountId = fromAccountId,
        toAccountId = toAccountId,
        amount = Amount(amount),
        imagePath = "",
        type = type,
        createdOn = due,
        updatedOn = due,
    )
    category?.let { transaction.category = it }
    fromAccount?.let { transaction.fromAccount = it }
    transaction.toAccount = toAccount
    return transaction
}

// A year of a daily rule; enough for any range a person would browse.
private const val MAX_PER_RULE = 400
