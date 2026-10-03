package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.model.LoggingStreak
import com.naveenapps.expensemanager.core.repository.FeedbackRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Live logging streak, combining days with transactions and confirmed no-spend days.
 * The calculation itself is [calculateStreak], a pure function that is unit-tested.
 */
class GetLoggingStreakUseCase(
    private val transactionRepository: TransactionRepository,
    private val feedbackRepository: FeedbackRepository,
) {
    operator fun invoke(): Flow<LoggingStreak> = combine(
        transactionRepository.getAllTransaction(),
        feedbackRepository.getNoSpendDays(),
    ) { transactions, noSpendDays ->
        val zone = ZoneId.systemDefault()
        val transactionDays = transactions.orEmpty()
            .map { it.createdOn.toInstant().atZone(zone).toLocalDate().toEpochDay() }
        calculateStreak(
            loggedDays = (transactionDays + noSpendDays).toSet(),
            today = LocalDate.now().toEpochDay(),
        )
    }
}

/**
 * @param loggedDays epoch days (LocalDate.toEpochDay) that count as logged.
 * @param today epoch day of "today".
 */
fun calculateStreak(loggedDays: Set<Long>, today: Long): LoggingStreak {
    val loggedToday = today in loggedDays

    // Today isn't over: an unlogged today doesn't break the streak, so count back from yesterday.
    var day = if (loggedToday) today else today - 1
    var current = 0
    while (day in loggedDays) {
        current++
        day--
    }

    var longest = 0
    var run = 0
    var previous: Long? = null
    for (d in loggedDays.filter { it <= today }.sorted()) {
        run = if (previous != null && d == previous + 1) run + 1 else 1
        longest = maxOf(longest, run)
        previous = d
    }

    return LoggingStreak(
        current = current,
        longest = maxOf(longest, current),
        loggedToday = loggedToday,
        lastSevenDays = (6 downTo 0).map { (today - it) in loggedDays },
    )
}
