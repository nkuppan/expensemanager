package com.naveenapps.expensemanager.core.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.naveenapps.expensemanager.core.model.AutoBackupError
import com.naveenapps.expensemanager.core.model.AutoBackupResult
import com.naveenapps.expensemanager.core.model.BackupKind
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.AutoBackupRepository
import kotlinx.coroutines.flow.first

/** Periodic automatic backup into the folder chosen in Settings → Backup (or the app's storage). */
class AutoBackupWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val autoBackupRepository: AutoBackupRepository,
    private val notificationScheduler: NotificationScheduler,
    private val analyticsRepository: AnalyticsRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // A stale schedule (e.g. turned off while this run was already queued) does nothing.
        if (!autoBackupRepository.getSettings().first().isEnabled) return Result.success()

        return when (val result = autoBackupRepository.backupNow(BackupKind.AUTO)) {
            AutoBackupResult.SUCCESS -> {
                notificationScheduler.cancelAutoBackupFailedNotification()
                analyticsRepository.logEvent(AnalyticsEvents.AUTO_BACKUP_COMPLETED, mapOf(AnalyticsParams.SOURCE to "background"))
                Result.success()
            }

            AutoBackupResult.SKIPPED -> Result.success()

            // Retrying can't fix a missing folder; the error is shown in Settings until the user
            // picks the folder again. The next period still runs.
            AutoBackupResult.FOLDER_UNAVAILABLE -> {
                logFailure(result)
                notifyOnce(AutoBackupError.FOLDER_UNAVAILABLE)
                Result.success()
            }

            AutoBackupResult.FAILED -> {
                logFailure(result)
                if (runAttemptCount < MAX_RETRIES) {
                    Result.retry()
                } else {
                    // Only once the retries are used up: a one-off glitch shouldn't alarm anyone.
                    notifyOnce(AutoBackupError.WRITE_FAILED)
                    Result.success()
                }
            }
        }
    }

    /**
     * One notification per run of failures, not one per day while it stays broken. Home keeps
     * showing a banner until it's fixed, so the user isn't left without a reminder.
     */
    private suspend fun notifyOnce(error: AutoBackupError) {
        if (autoBackupRepository.getSettings().first().isFailureNotified) return
        notificationScheduler.showAutoBackupFailedNotification(error)
        autoBackupRepository.markFailureNotified()
    }

    private fun logFailure(result: AutoBackupResult) {
        analyticsRepository.logEvent(
            AnalyticsEvents.AUTO_BACKUP_FAILED,
            mapOf(AnalyticsParams.SOURCE to "background", AnalyticsParams.REASON to result.name.lowercase()),
        )
    }

    private companion object {
        const val MAX_RETRIES = 3
    }
}
