package com.naveenapps.expensemanager.core.domain.usecase.recurring

import com.naveenapps.expensemanager.core.model.RecurringFrequency
import com.naveenapps.expensemanager.core.model.RecurringTransaction
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.RecurringTransactionRepository
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Plans a transaction (not saved) to repeat every frequency from its own date, which is on a later day
 * ([isOnLaterDay]). Unlike [CreateRecurringTransactionUseCase] nothing is saved now and no
 * balance changes: occurrence 0 is the start date itself, and the processor logs it on that day
 * like any other occurrence (with the deterministic occurrence id, so it's never doubled).
 */
class PlanRecurringTransactionUseCase(
    private val repository: RecurringTransactionRepository,
) {
    suspend operator fun invoke(
        transaction: Transaction,
        frequency: RecurringFrequency,
        now: Date = Date(),
    ) {
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
                nextOccurrence = 0,
                nextDueDate = transaction.createdOn,
                createdOn = now,
                updatedOn = now,
            ),
        )
    }
}

/** What's planned: every rule soonest first, and what they add up to over the next [windowDays]. */
data class UpcomingRecurring(
    val rules: List<RecurringTransaction> = emptyList(),
    /** Sum of every expense occurrence due in the window (a weekly rule counts 4 or 5 times). */
    val expenseDue: Double = 0.0,
    val incomeDue: Double = 0.0,
    val windowDays: Int = UPCOMING_WINDOW_DAYS,
)

const val UPCOMING_WINDOW_DAYS = 30

/** The dashboard's "Upcoming" section. Recomputed whenever a rule or its category / account changes. */
class GetUpcomingRecurringUseCase(
    private val getRecurringTransactionsUseCase: GetRecurringTransactionsUseCase,
) {
    operator fun invoke(
        now: () -> Date = { Date() },
        zone: ZoneId = ZoneId.systemDefault(),
    ): Flow<UpcomingRecurring> = getRecurringTransactionsUseCase().map { rules ->
        summarizeUpcoming(rules, now(), zone)
    }
}

/**
 * Rules soonest first, plus the totals of every occurrence due from now to the end of the day
 * [windowDays] ahead. An occurrence already due but not yet logged (the app was closed) counts:
 * it's logged on the next app start. Transfers move money between accounts, so they're left out
 * of both totals.
 */
internal fun summarizeUpcoming(
    rules: List<RecurringTransaction>,
    now: Date,
    zone: ZoneId,
    windowDays: Int = UPCOMING_WINDOW_DAYS,
): UpcomingRecurring {
    val windowEnd = now.toInstant().atZone(zone).toLocalDate()
        .plusDays(windowDays.toLong() + 1)
        .atStartOfDay(zone)
        .toInstant()
        .let { Date(it.toEpochMilli() - 1) }
    var expense = 0.0
    var income = 0.0
    rules.forEach { rule ->
        if (rule.type == TransactionType.TRANSFER) return@forEach
        var index = rule.nextOccurrence
        var due = rule.nextDueDate
        var counted = 0
        while (!due.after(windowEnd) && counted < MAX_OCCURRENCES_IN_WINDOW) {
            if (rule.type == TransactionType.EXPENSE) expense += rule.amount else income += rule.amount
            counted++
            index++
            due = occurrenceDate(rule.startDate, rule.frequency, index, zone)
        }
    }
    return UpcomingRecurring(
        rules = rules.sortedBy { it.nextDueDate },
        expenseDue = expense,
        incomeDue = income,
        windowDays = windowDays,
    )
}

// A daily rule overdue for a long time can't make the loop run away.
private const val MAX_OCCURRENCES_IN_WINDOW = 400

/** Frequencies that get a heads-up the evening before; daily ones would notify every evening. */
val HEADS_UP_FREQUENCIES = setOf(RecurringFrequency.WEEKLY, RecurringFrequency.MONTHLY, RecurringFrequency.YEARLY)

/**
 * Weekly, monthly and yearly rules whose next entry is added on [day], soonest first. The evening
 * heads-up asks for tomorrow's. A rule that's already overdue isn't included: it's logged on the
 * next app start or background run, not on [day].
 */
fun rulesAddedOn(
    rules: List<RecurringTransaction>,
    day: LocalDate,
    zone: ZoneId = ZoneId.systemDefault(),
): List<RecurringTransaction> = rules
    .filter { it.frequency in HEADS_UP_FREQUENCIES }
    .filter { it.nextDueDate.toInstant().atZone(zone).toLocalDate() == day }
    .sortedBy { it.nextDueDate }
