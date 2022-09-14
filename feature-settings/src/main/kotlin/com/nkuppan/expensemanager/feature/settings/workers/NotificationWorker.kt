package com.nkuppan.expensemanager.feature.settings.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.nkuppan.expensemanager.feature.settings.utils.NotificationScheduler

class NotificationWorker(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {

        /*NotificationScheduler.showNotification(
            context,
            HomeActivity::class.java,
            context.getString(R.string.notification_title),
            context.getString(R.string.add_transaction)
        )*/

        NotificationScheduler.setReminder(context)

        return Result.success()
    }
}