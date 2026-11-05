package com.naveenapps.expensemanager.core.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.naveenapps.expensemanager.core.domain.usecase.recurring.GetRecurringTransactionsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.recurring.rulesAddedOn
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.RecurringTransaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.ReminderTimeRepository
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first

/**
 * Runs at [UPCOMING_ALERT_HOUR] each evening: if any weekly, monthly or yearly recurring entry is added
 * tomorrow, shows one heads-up ("Tomorrow: Rent", "₹15,000 will be added automatically"), or a
 * list when there are several. Then schedules tomorrow evening's run. Stays quiet when the user
 * switched "Upcoming payment alerts" off, and never notifies twice on the same evening.
 */
class UpcomingRecurringWorker(
    private val context: Context,
    workerParams: WorkerParameters,
    private val getRecurringTransactionsUseCase: GetRecurringTransactionsUseCase,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val reminderTimeRepository: ReminderTimeRepository,
    private val notificationScheduler: NotificationScheduler,
    private val analyticsRepository: AnalyticsRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (!reminderTimeRepository.isUpcomingAlertOn().first()) return Result.success()
        try {
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)
            if (reminderTimeRepository.getLastUpcomingAlertDay().first() != today.toEpochDay()) {
                val tomorrow = rulesAddedOn(getRecurringTransactionsUseCase().first(), today.plusDays(1), zone)
                if (tomorrow.isNotEmpty()) {
                    notifyAbout(tomorrow)
                    reminderTimeRepository.setLastUpcomingAlertDay(today.toEpochDay())
                    analyticsRepository.logEvent(
                        AnalyticsEvents.UPCOMING_ALERT_SHOWN,
                        mapOf(AnalyticsParams.COUNT to tomorrow.size.toString()),
                    )
                }
            }
        } finally {
            // Always line up tomorrow evening's check, even if this one failed.
            notificationScheduler.scheduleUpcomingAlert()
        }
        return Result.success()
    }

    private suspend fun notifyAbout(rules: List<RecurringTransaction>) {
        val currency = getCurrencyUseCase.invoke().first()
        fun amountOf(rule: RecurringTransaction): String {
            val formatted = getFormattedAmountUseCase.invoke(rule.amount, currency).amountString.orEmpty()
            return if (rule.type == TransactionType.INCOME) "+$formatted" else formatted
        }

        val single = rules.singleOrNull()
        if (single != null) {
            notificationScheduler.showUpcomingRecurringNotification(
                title = context.getString(R.string.upcoming_alert_title_one, nameOf(single)),
                lines = listOf(context.getString(R.string.upcoming_alert_body_one, amountOf(single))),
                summary = "",
            )
        } else {
            notificationScheduler.showUpcomingRecurringNotification(
                title = context.resources.getQuantityString(
                    R.plurals.upcoming_alert_title_many,
                    rules.size,
                    rules.size,
                ),
                lines = rules.map { context.getString(R.string.upcoming_alert_line, nameOf(it), amountOf(it)) },
                summary = context.getString(R.string.upcoming_alert_body_many),
            )
        }
    }

    /** The notes ("Rent") when there are any, otherwise the category's (translated) name. */
    private fun nameOf(rule: RecurringTransaction): String = rule.notes.ifBlank {
        rule.category?.let { category ->
            category.titleResId?.let { context.getString(it) } ?: category.name
        }.orEmpty()
    }
}
