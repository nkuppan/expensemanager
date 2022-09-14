package com.nkuppan.expensemanager.feature.settings.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.nkuppan.expensemanager.feature.settings.R
import com.nkuppan.expensemanager.feature.settings.workers.NotificationWorker
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs


object NotificationScheduler {

    internal fun setReminder(context: Context) {

        WorkManager.getInstance(context).cancelAllWork()

        val calendar = getTimeInMillis(context)
        val lTimeDelay = abs(Date().time - calendar.timeInMillis)

        val request = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(
                lTimeDelay,
                TimeUnit.MILLISECONDS
            ).build()

        WorkManager.getInstance(context).enqueue(request)
    }

    internal fun cancelReminder(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
    }

    private fun getTimeInMillis(context: Context): Calendar {

        /*val lHour = PreferenceManager.getAlarmTimeHour(context)
        val lMinute = PreferenceManager.getAlarmTimeMinute(context)*/

        val hour = 0
        val minute = 0

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

    fun showNotification(
        context: Context,
        destinationClass: Class<*>,
        title: String,
        content: String
    ) {

        createChannelIfRequired(context)

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationIntent = Intent(context, destinationClass)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        notificationIntent.action = "add_transaction"

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(destinationClass)
        stackBuilder.addNextIntent(notificationIntent)

        val pendingIntent = stackBuilder.getPendingIntent(
            NotificationId.DAILY_REMINDER_REQUEST_CODE,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, NotificationChannelId.CHANNEL_GENERAL)

        val notification = builder.setContentTitle(title)
            .setContentText(content).setAutoCancel(true)
            .setSound(alarmSound).setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent).build()

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        notificationManager.notify(NotificationId.DAILY_REMINDER_REQUEST_CODE, notification)
    }

    private fun createChannelIfRequired(aContext: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(
                aContext,
                NotificationChannelId.CHANNEL_GENERAL,
                NotificationChannelId.CHANNEL_GENERAL
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        context: Context,
        channelId: String,
        channelName: String
    ): String {

        val notificationChannel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )

        notificationChannel.lightColor = Color.BLUE
        notificationChannel.lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
        val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(notificationChannel)

        return channelId
    }
}

object NotificationId {
    const val DAILY_REMINDER_REQUEST_CODE = 101
}

object NotificationChannelId {
    const val CHANNEL_GENERAL = "general"
}

open class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action != null) {

            if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
                // Set the alarm here.
                Log.d(TAG, "onReceive: BOOT_COMPLETED")
                NotificationScheduler.setReminder(context)
                return
            }
        }

        //Trigger the notification
        /*NotificationScheduler.showNotification(
            context,
            HomeActivity::class.java,
            context.getString(R.string.notification_title),
            context.getString(R.string.add_transaction)
        )*/
    }

    companion object {
        private const val TAG = "AlarmReceiver"
    }
}
