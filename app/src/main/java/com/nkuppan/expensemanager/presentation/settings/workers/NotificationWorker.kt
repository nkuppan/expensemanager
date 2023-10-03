package com.nkuppan.expensemanager.presentation.settings.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.presentation.HomeActivity
import com.nkuppan.expensemanager.presentation.settings.utils.NotificationScheduler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationWorker(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @OptIn(DelicateCoroutinesApi::class)
    override fun doWork(): Result {

        notificationScheduler.showNotification(
            context,
            HomeActivity::class.java,
            context.getString(R.string.notification_title),
            context.getString(R.string.add_transaction)
        )

        GlobalScope.launch {
            notificationScheduler.setReminder(context)
        }

        return Result.success()
    }
}