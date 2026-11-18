package com.naveenapps.expensemanager.core.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.FeedbackRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.first

/**
 * The daily reminder. Every run reports what happened, so delivery can be checked in Analytics:
 * reminder_shown, reminder_skipped_already_logged or reminder_not_shown (with a reason), each
 * with late_minutes (how long after the chosen time it ran; Android may defer background work).
 * A run that never happens at all is caught on the next app start as reminder_missed.
 */
class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters,
    private val notificationScheduler: NotificationScheduler,
    private val feedbackRepository: FeedbackRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val reminderQuickLog: ReminderQuickLog,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        notificationScheduler.markReminderRun(now)
        val dueAt = inputData.getLong(KEY_DUE_AT, 0L)
        val timing = if (dueAt > 0L) {
            mapOf(AnalyticsParams.LATE_MINUTES to ((now - dueAt) / MILLIS_PER_MINUTE).coerceAtLeast(0).toString())
        } else {
            emptyMap()
        }

        val today = LocalDate.now().toEpochDay()
        if (feedbackRepository.getLastActiveDay().first() == today) {
            // Already logged something (or confirmed a no-spend day) today: a reminder now is
            // just noise, and noisy reminders are the ones people switch off.
            analyticsRepository.logEvent(AnalyticsEvents.REMINDER_SKIPPED_ALREADY_LOGGED, timing)
        } else {
            // The user's most repeated expense as a one-tap button, when there is one.
            val quickPick = runCatching { reminderQuickLog.topPick() }.getOrNull()
            val block = notificationScheduler.showNotification(
                destinationClass = DESTINATION_CLASS,
                title = context.getString(R.string.reminder_notification_title),
                content = context.getString(R.string.reminder_notification_body),
                quickPick = quickPick,
            )
            if (block == null) {
                analyticsRepository.logEvent(
                    AnalyticsEvents.REMINDER_SHOWN,
                    timing + (AnalyticsParams.COUNT to (if (quickPick != null) 1 else 0).toString()),
                )
            } else {
                analyticsRepository.logEvent(
                    AnalyticsEvents.REMINDER_NOT_SHOWN,
                    timing + (AnalyticsParams.REASON to block.analyticsValue),
                )
            }
        }

        // Always schedule tomorrow's reminder.
        notificationScheduler.checkAndRestartReminder()

        return Result.success()
    }

    companion object {
        /** When the reminder was due (epoch millis), set by NotificationScheduler.setReminder. */
        const val KEY_DUE_AT = "due_at"
        private const val MILLIS_PER_MINUTE = 60_000L
    }
}
