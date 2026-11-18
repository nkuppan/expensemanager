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
 * The daily reminder's background buttons; the app doesn't open for any of them.
 *
 * - "Nothing spent today": records today as a no-spend day (so it counts as logged), then
 *   dismisses the notification.
 * - A quick pick ("Tea ₹20"): saves that expense, and the notification turns into
 *   "Logged Tea ₹20" with Undo for a minute.
 * - "Undo": deletes that expense again and dismisses the notification.
 */
class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != ACTION_NO_SPEND_TODAY && action != ACTION_QUICK_LOG && action != ACTION_UNDO_QUICK_LOG) return

        val koin = GlobalContext.getOrNull() ?: return

        // goAsync keeps the receiver alive until the database / DataStore work finishes.
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                when (action) {
                    ACTION_NO_SPEND_TODAY -> {
                        NotificationManagerCompat.from(context).cancel(NotificationId.DAILY_REMINDER_REQUEST_CODE)
                        koin.get<FeedbackRepository>().recordNoSpendDay(LocalDate.now().toEpochDay())
                        koin.get<AnalyticsRepository>().logEvent(AnalyticsEvents.REMINDER_NO_SPEND, emptyMap())
                    }

                    ACTION_QUICK_LOG -> {
                        val scheduler = koin.get<NotificationScheduler>()
                        val pick = ReminderQuickPick.from(intent)
                        val transactionId = pick?.let { koin.get<ReminderQuickLog>().log(it) }
                        if (pick != null && transactionId != null) {
                            scheduler.showQuickLogged(pick.label, transactionId)
                        } else {
                            // Its category or account is gone: offer the add screen instead.
                            scheduler.showQuickLogFailed()
                        }
                    }

                    ACTION_UNDO_QUICK_LOG -> {
                        NotificationManagerCompat.from(context).cancel(NotificationId.DAILY_REMINDER_REQUEST_CODE)
                        intent.getStringExtra(EXTRA_TRANSACTION_ID)?.let { koin.get<ReminderQuickLog>().undo(it) }
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_NO_SPEND_TODAY =
            "com.naveenapps.expensemanager.core.notification.NO_SPEND_TODAY"
        const val ACTION_QUICK_LOG =
            "com.naveenapps.expensemanager.core.notification.QUICK_LOG"
        const val ACTION_UNDO_QUICK_LOG =
            "com.naveenapps.expensemanager.core.notification.UNDO_QUICK_LOG"
        const val EXTRA_TRANSACTION_ID = "transaction_id"
    }
}
