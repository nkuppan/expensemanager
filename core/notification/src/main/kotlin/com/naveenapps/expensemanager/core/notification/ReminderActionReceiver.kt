package com.naveenapps.expensemanager.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.FeedbackRepository
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

/**
 * Handles the daily reminder's "Nothing spent today" button without opening the app:
 * records today as a no-spend day (so it counts as logged, and later towards the streak),
 * then dismisses the notification.
 */
class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_NO_SPEND_TODAY) return

        NotificationManagerCompat.from(context).cancel(NotificationId.DAILY_REMINDER_REQUEST_CODE)

        val koin = GlobalContext.getOrNull() ?: return
        val feedbackRepository: FeedbackRepository = koin.get()
        val analyticsRepository: AnalyticsRepository = koin.get()

        // goAsync keeps the receiver alive until the DataStore write finishes.
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                feedbackRepository.recordNoSpendDay(LocalDate.now().toEpochDay())
                analyticsRepository.logEvent(AnalyticsEvents.REMINDER_NO_SPEND, emptyMap())
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_NO_SPEND_TODAY =
            "com.naveenapps.expensemanager.core.notification.NO_SPEND_TODAY"
    }
}
