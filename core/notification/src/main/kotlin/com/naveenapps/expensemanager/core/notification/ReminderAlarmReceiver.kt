package com.naveenapps.expensemanager.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

/**
 * The daily reminder's alarm went off (not exported: only our own PendingIntent reaches it).
 * The reminder itself (checks, notification, analytics, next day's alarm) runs in
 * [NotificationWorker], started right away as expedited work, which Doze doesn't hold back the
 * way it does delayed background work.
 */
class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_REMINDER_ALARM) return
        runReminderNow(context, intent.getLongExtra(EXTRA_DUE_AT, 0L))
    }

    private fun runReminderNow(context: Context, dueAt: Long) {
        val request = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(workDataOf(NotificationWorker.KEY_DUE_AT to dueAt))
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        try {
            WorkManager.getInstance(context)
                .enqueueUniqueWork(REMINDER_RUN_WORK_NAME, ExistingWorkPolicy.KEEP, request)
        } catch (error: Throwable) {
            // WorkManager can be missing on broken API 34 builds (see NotificationScheduler).
            Log.w(TAG, "Couldn't start the reminder", error)
        }
    }

    companion object {
        const val ACTION_REMINDER_ALARM = "com.naveenapps.expensemanager.core.notification.REMINDER_ALARM"
        const val EXTRA_DUE_AT = "due_at"

        /** Separate from the old delayed-work name, which setReminder cancels. */
        const val REMINDER_RUN_WORK_NAME = "daily_reminder_run"
        private const val TAG = "ReminderAlarmReceiver"
    }
}

/**
 * Boot, app update, clock or time-zone change: Android drops or misplaces alarms then, so the
 * next reminder is set again. Exported only because these broadcasts come from the system; they
 * are protected, so no other app can send them.
 */
class ReminderRescheduleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            -> reschedule()
        }
    }

    private fun reschedule() {
        val scheduler = GlobalContext.getOrNull()?.getOrNull<NotificationScheduler>() ?: return
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                scheduler.checkAndRestartReminder()
            } finally {
                pending.finish()
            }
        }
    }
}
