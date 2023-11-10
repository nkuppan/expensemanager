package com.naveenapps.expensemanager.core.notification

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationWorker(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    override fun doWork(): Result {

        notificationScheduler.showNotification(
            context,
            DESTINATION_CLASS,
            context.getString(R.string.notification_description),
            context.getString(R.string.notification_title)
        )

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            notificationScheduler.setReminder(context)
        }

        return Result.success()
    }
}