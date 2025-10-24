package com.naveenapps.expensemanager.core.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters,
    private val notificationScheduler: NotificationScheduler
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        notificationScheduler.showNotification(
            DESTINATION_CLASS,
            context.getString(R.string.notification_description),
            context.getString(R.string.notification_title),
        )

        notificationScheduler.setReminder()

        return Result.success()
    }
}
