package com.naveenapps.expensemanager.core.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

/**
 * Sunday-evening "your week in money": this week's spending (Monday to now) against last week's.
 * Silent when there's nothing to say, and respects the daily-reminder switch as the user's
 * "notifications from this app" opt-out.
 */
class WeeklySummaryWorker(
    private val context: Context,
    workerParams: WorkerParameters,
    private val notificationScheduler: NotificationScheduler,
    private val transactionRepository: TransactionRepository,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val analyticsRepository: AnalyticsRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (!notificationScheduler.isReminderSwitchOn()) return Result.success()

        val zone = ZoneId.systemDefault()
        val thisWeekStart = LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay(zone).toInstant().toEpochMilli()
        val lastWeekStart = thisWeekStart - WEEK_MILLIS
        val now = System.currentTimeMillis()

        val expenses = transactionRepository.getAllTransaction().firstOrNull().orEmpty()
            .filter { it.type == TransactionType.EXPENSE }
        val thisWeek = expenses.filter { it.createdOn.time in thisWeekStart..now }
        val lastWeek = expenses.filter { it.createdOn.time in lastWeekStart until thisWeekStart }

        val thisTotal = thisWeek.sumOf { it.amount.amount }
        val lastTotal = lastWeek.sumOf { it.amount.amount }
        if (thisTotal <= 0.0 && lastTotal <= 0.0) return Result.success()

        val currency = getCurrencyUseCase.invoke().first()
        val spent = getFormattedAmountUseCase.invoke(thisTotal, currency).amountString.orEmpty()

        val message = when {
            lastTotal <= 0.0 -> context.getString(R.string.weekly_summary_first, spent)

            else -> {
                val change = ((thisTotal - lastTotal) / lastTotal * 100).roundToInt()
                when {
                    abs(change) < SAME_THRESHOLD_PERCENT ->
                        context.getString(R.string.weekly_summary_same, spent)

                    change < 0 -> context.getString(R.string.weekly_summary_less, spent, abs(change))

                    else -> context.getString(R.string.weekly_summary_more, spent, change)
                }
            }
        }

        notificationScheduler.showInsightNotification(
            notificationId = NotificationId.WEEKLY_SUMMARY,
            title = context.getString(R.string.weekly_summary_title),
            content = message,
        )
        analyticsRepository.logEvent(AnalyticsEvents.WEEKLY_SUMMARY_SHOWN, emptyMap())
        return Result.success()
    }

    private companion object {
        const val WEEK_MILLIS = 7L * 24 * 60 * 60 * 1000

        // Within ±5% reads as "about the same" rather than a precise-sounding 2% change.
        const val SAME_THRESHOLD_PERCENT = 5
    }
}
