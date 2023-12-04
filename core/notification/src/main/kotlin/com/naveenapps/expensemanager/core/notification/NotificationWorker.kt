package com.naveenapps.expensemanager.core.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationScheduler: NotificationScheduler,
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
