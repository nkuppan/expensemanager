package com.naveenapps.expensemanager.core.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.naveenapps.expensemanager.core.repository.BudgetAlertTrigger
import com.naveenapps.expensemanager.core.repository.ReminderTimeRepository
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

private const val TAG = "NotificationScheduler"

class NotificationScheduler(
    private val context: Context,
    private val reminderTimeRepository: ReminderTimeRepository,
) : BudgetAlertTrigger {

    /**
     * Weekly "your week in money" summary, every Sunday at 19:00 local time. KEEP, so calling
     * this on every app start never pushes the next run back.
     */
    fun scheduleWeeklySummary() {
        val now = LocalDateTime.now()
        var next = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            .withHour(WEEKLY_SUMMARY_HOUR).withMinute(0).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusWeeks(1)

        val request = PeriodicWorkRequestBuilder<WeeklySummaryWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(Duration.between(now, next).toMillis(), TimeUnit.MILLISECONDS)
            .build()

        withWorkManager {
            it.enqueueUniquePeriodicWork(
                WEEKLY_SUMMARY_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }

    /** Called after a transaction is saved; a short delay batches quick successive saves. */
    override fun checkBudgetsSoon() {
        val request = OneTimeWorkRequestBuilder<BudgetAlertWorker>()
            .setInitialDelay(BUDGET_CHECK_DELAY_SECONDS, TimeUnit.SECONDS)
            .build()
        withWorkManager {
            it.enqueueUniqueWork(BUDGET_ALERT_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }

    /** OS-level permission only (Android 13+), independent of the daily-reminder switch. */
    fun hasNotificationPermission(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

    /** Daily-reminder switch from Settings: also used as the "notifications from the app" opt-in. */
    suspend fun isReminderSwitchOn(): Boolean = reminderTimeRepository.isReminderOn().firstOrNull() ?: false

    /**
     * Informational notification (weekly summary, budget alerts). Tapping it opens the app.
     * Callers decide whether it should be shown; this only checks the OS permission.
     */
    // Permission is checked by hasNotificationPermission() just below; lint can't see through it.
    @SuppressLint("MissingPermission")
    fun showInsightNotification(notificationId: Int, title: String, content: String) {
        if (!hasNotificationPermission()) return
        createChannelIfRequired()

        val openApp = Intent(context, Class.forName(DESTINATION_CLASS))
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, NotificationChannelId.CHANNEL_GENERAL)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setSmallIcon(R.drawable.account_balance_wallet)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        runCatching { NotificationManagerCompat.from(context).notify(notificationId, notification) }
    }

    suspend fun checkAndRestartReminder() {
        if (canWeShowNotification().not()) {
            return
        }

        setReminder()
    }

    suspend fun setReminder() {
        val calendar = getTimeInMillis()
        val timeDelay = calendar.timeInMillis - Date().time

        val request = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(timeDelay, TimeUnit.MILLISECONDS)
            .build()

        withWorkManager {
            it.enqueueUniqueWork(REMINDER_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }

    fun cancelReminder() {
        withWorkManager { it.cancelUniqueWork(REMINDER_WORK_NAME) }
    }

    /**
     * WorkManager initialises lazily on the first getInstance() call. On devices that report
     * API 34+ without the matching framework (JobScheduler.forNamespace missing), that throws
     * NoSuchMethodError — an Error, not an Exception — so catch Throwable here. Losing the
     * reminder on such a device is far better than crashing the app.
     */
    private inline fun withWorkManager(block: (WorkManager) -> Unit) {
        try {
            block(WorkManager.getInstance(context))
        } catch (error: Throwable) {
            Log.w(TAG, "WorkManager unavailable on this device; reminder not scheduled", error)
        }
    }

    private suspend fun getTimeInMillis(): Calendar {
        val reminderTimeState = reminderTimeRepository.getReminderTime().first()

        val hour = reminderTimeState.hour
        val minute = reminderTimeState.minute

        val calendar = Calendar.getInstance()

        val setCalendar = Calendar.getInstance()
        setCalendar.set(Calendar.HOUR_OF_DAY, hour)
        setCalendar.set(Calendar.MINUTE, minute)
        setCalendar.set(Calendar.SECOND, 0)

        if (setCalendar.before(calendar)) {
            setCalendar.add(Calendar.DATE, 1)
        }

        return setCalendar
    }

    suspend fun showNotification(
        destinationClass: String,
        title: String,
        content: String,
    ) {
        if (canWeShowNotification().not()) {
            return
        }

        createChannelIfRequired()

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationIntent = Intent(context, Class.forName(destinationClass))
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        notificationIntent.action = "add_transaction"

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(Class.forName(destinationClass))
        stackBuilder.addNextIntent(notificationIntent)

        val pendingIntent = stackBuilder.getPendingIntent(
            NotificationId.DAILY_REMINDER_REQUEST_CODE,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // "Nothing spent today": handled in the background, the app doesn't open.
        val noSpendIntent = PendingIntent.getBroadcast(
            context,
            NotificationId.NO_SPEND_REQUEST_CODE,
            Intent(context, ReminderActionReceiver::class.java)
                .setAction(ReminderActionReceiver.ACTION_NO_SPEND_TODAY),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(context, NotificationChannelId.CHANNEL_GENERAL)

        val notification = builder.setContentTitle(title)
            .setContentText(content).setAutoCancel(true)
            .setSound(alarmSound)
            .setSmallIcon(R.drawable.account_balance_wallet)
            .setContentIntent(pendingIntent)
            // "Add expense" = same as tapping: the app opens straight on the keypad.
            .addAction(0, context.getString(R.string.reminder_action_add), pendingIntent)
            .addAction(0, context.getString(R.string.reminder_action_no_spend), noSpendIntent)
            .build()

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        notificationManager.notify(
            NotificationId.DAILY_REMINDER_REQUEST_CODE,
            notification,
        )
    }

    private suspend fun canWeShowNotification(): Boolean {
        val isReminderOn = reminderTimeRepository.isReminderOn().firstOrNull() ?: false

        if (isReminderOn.not()) {
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }

        return true
    }

    private fun createChannelIfRequired() {
        createNotificationChannel(
            NotificationChannelId.CHANNEL_GENERAL,
            NotificationChannelId.CHANNEL_GENERAL,
            context.getString(R.string.notification_channel_description),
        )
    }

    private fun createNotificationChannel(
        channelId: String,
        channelName: String,
        channelDescription: String,
    ) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = channelDescription
            lightColor = Color.BLUE
        }

        NotificationManagerCompat
            .from(context)
            .createNotificationChannel(channel)
    }
}

object NotificationId {
    const val DAILY_REMINDER_REQUEST_CODE = 101
    const val NO_SPEND_REQUEST_CODE = 102
    const val WEEKLY_SUMMARY = 103

    /** Budget alerts use this base plus a per-budget offset, so each budget has its own. */
    const val BUDGET_ALERT_BASE = 10_000
}

private const val REMINDER_WORK_NAME = "daily_reminder"
private const val WEEKLY_SUMMARY_WORK_NAME = "weekly_summary"
private const val BUDGET_ALERT_WORK_NAME = "budget_alert_check"
private const val WEEKLY_SUMMARY_HOUR = 19
private const val BUDGET_CHECK_DELAY_SECONDS = 5L

object NotificationChannelId {
    const val CHANNEL_GENERAL = "General"
}
