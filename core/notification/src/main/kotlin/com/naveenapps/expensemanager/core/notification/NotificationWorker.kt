package com.naveenapps.expensemanager.core.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.FeedbackRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.first

class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters,
    private val notificationScheduler: NotificationScheduler,
    private val feedbackRepository: FeedbackRepository,
    private val analyticsRepository: AnalyticsRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now().toEpochDay()
        if (feedbackRepository.getLastActiveDay().first() == today) {
            // Already logged something (or confirmed a no-spend day) today: a reminder now is
            // just noise, and noisy reminders are the ones people switch off.
            analyticsRepository.logEvent(AnalyticsEvents.REMINDER_SKIPPED_ALREADY_LOGGED, emptyMap())
        } else {
            notificationScheduler.showNotification(
                destinationClass = DESTINATION_CLASS,
                title = context.getString(R.string.reminder_notification_title),
                content = context.getString(R.string.reminder_notification_body),
            )
            analyticsRepository.logEvent(AnalyticsEvents.REMINDER_SHOWN, emptyMap())
        }

        // Always schedule tomorrow's reminder.
        notificationScheduler.checkAndRestartReminder()

        return Result.success()
    }
}
