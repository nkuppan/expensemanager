package com.naveenapps.expensemanager.core.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.naveenapps.expensemanager.core.repository.ReminderTimeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class NotificationScheduler(
    private val context: Context,
    private val reminderTimeRepository: ReminderTimeRepository,
) {

    suspend fun checkAndRestartReminder() {
        if (canWeShowNotification().not()) {
            return
        }

        setReminder()
    }

    suspend fun setReminder() {
        cancelReminder()

        val calendar = getTimeInMillis()
        val timeDelay = abs(Date().time - calendar.timeInMillis)

        val request = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(
                timeDelay,
                TimeUnit.MILLISECONDS,
            ).build()

        WorkManager.getInstance(context).enqueue(request)
    }

    fun cancelReminder() {
        WorkManager.getInstance(context).cancelAllWork()
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

        val builder = NotificationCompat.Builder(context, NotificationChannelId.CHANNEL_GENERAL)

        val notification = builder.setContentTitle(title)
            .setContentText(content).setAutoCancel(true)
            .setSound(alarmSound)
            .setSmallIcon(R.drawable.account_balance_wallet)
            .setContentIntent(pendingIntent).build()

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(
                NotificationChannelId.CHANNEL_GENERAL,
                NotificationChannelId.CHANNEL_GENERAL,
                "Used for reminders",
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
}

object NotificationChannelId {
    const val CHANNEL_GENERAL = "General"
}
