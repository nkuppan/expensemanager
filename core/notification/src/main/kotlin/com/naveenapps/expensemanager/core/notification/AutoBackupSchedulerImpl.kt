package com.naveenapps.expensemanager.core.notification

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.naveenapps.expensemanager.core.model.AutoBackupFrequency
import com.naveenapps.expensemanager.core.repository.AutoBackupScheduler
import java.util.concurrent.TimeUnit

class AutoBackupSchedulerImpl(
    private val context: Context,
) : AutoBackupScheduler {

    override fun schedule(frequency: AutoBackupFrequency, replaceExisting: Boolean) {
        val request = PeriodicWorkRequestBuilder<AutoBackupWorker>(frequency.intervalDays, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .setRequiresStorageNotLow(true)
                    .build(),
            )
            .build()
        // UPDATE keeps the current period's timing while applying the new interval; KEEP (app
        // start) never touches an existing schedule.
        val policy = if (replaceExisting) ExistingPeriodicWorkPolicy.UPDATE else ExistingPeriodicWorkPolicy.KEEP
        withWorkManager { it.enqueueUniquePeriodicWork(WORK_NAME, policy, request) }
    }

    override fun cancel() {
        withWorkManager { it.cancelUniqueWork(WORK_NAME) }
    }

    /** See NotificationScheduler.withWorkManager: some devices throw an Error on getInstance. */
    private inline fun withWorkManager(block: (WorkManager) -> Unit) {
        try {
            block(WorkManager.getInstance(context))
        } catch (error: Throwable) {
            Log.w(TAG, "WorkManager unavailable on this device; automatic backup not scheduled", error)
        }
    }

    private companion object {
        const val TAG = "AutoBackupScheduler"
        const val WORK_NAME = "auto_backup"
    }
}
