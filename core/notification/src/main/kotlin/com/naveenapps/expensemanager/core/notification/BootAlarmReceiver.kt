package com.naveenapps.expensemanager.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject


const val DESTINATION_CLASS = "com.naveenapps.expensemanager.presentation.HomeActivity"

open class BootAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {

            if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
                Log.d(TAG, "onReceive: BOOT_COMPLETED")
                notificationScheduler.setReminder(context)
                return@launch
            }

            notificationScheduler.showNotification(
                context,
                DESTINATION_CLASS,
                context.getString(R.string.notification_description),
                context.getString(R.string.notification_title)
            )
        }
    }

    companion object {
        private const val TAG = "AlarmReceiver"
    }
}