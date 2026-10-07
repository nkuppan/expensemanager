package com.naveenapps.expensemanager.core.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.naveenapps.expensemanager.core.domain.usecase.recurring.ProcessRecurringTransactionsUseCase
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository

/**
 * Logs recurring transactions that came due while the app wasn't opened, so balances and
 * budgets are right before the user looks. The app also runs the same use case on start.
 */
class RecurringTransactionWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val processRecurringTransactionsUseCase: ProcessRecurringTransactionsUseCase,
    private val notificationScheduler: NotificationScheduler,
    private val analyticsRepository: AnalyticsRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val created = processRecurringTransactionsUseCase.invoke()
        if (created > 0) {
            notificationScheduler.checkBudgetsSoon()
            analyticsRepository.logEvent(
                AnalyticsEvents.RECURRING_GENERATED,
                mapOf(AnalyticsParams.COUNT to created.toString(), AnalyticsParams.SOURCE to "background"),
            )
        }
        return Result.success()
    }
}
