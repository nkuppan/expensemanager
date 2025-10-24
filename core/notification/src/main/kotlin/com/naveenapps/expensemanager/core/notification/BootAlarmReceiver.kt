package com.naveenapps.expensemanager.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

const val DESTINATION_CLASS = "com.naveenapps.expensemanager.MainActivity"

open class BootAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val notificationScheduler: NotificationScheduler =  GlobalContext.get().get()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
                Log.d(TAG, "onReceive: BOOT_COMPLETED")
                notificationScheduler.setReminder()
                return@launch
            }

            notificationScheduler.showNotification(
                DESTINATION_CLASS,
                context.getString(R.string.notification_description),
                context.getString(R.string.notification_title),
            )
        }
    }

    companion object {
        private const val TAG = "AlarmReceiver"
    }
}
