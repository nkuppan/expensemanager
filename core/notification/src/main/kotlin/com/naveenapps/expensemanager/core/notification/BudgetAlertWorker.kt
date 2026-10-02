package com.naveenapps.expensemanager.core.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYearKey
import com.naveenapps.expensemanager.core.common.utils.toYear
import com.naveenapps.expensemanager.core.domain.usecase.budget.GetBudgetsUseCase
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.FeedbackRepository
import java.util.Date
import kotlin.math.abs
import kotlinx.coroutines.flow.first

/**
 * Runs shortly after a transaction is saved. For each budget covering the current month/year,
 * alerts once when spending reaches 80% and once when it reaches 100%. Sent alerts are
 * remembered per budget and period, so editing transactions never repeats an alert.
 */
class BudgetAlertWorker(
    private val context: Context,
    workerParams: WorkerParameters,
    private val notificationScheduler: NotificationScheduler,
    private val getBudgetsUseCase: GetBudgetsUseCase,
    private val feedbackRepository: FeedbackRepository,
    private val analyticsRepository: AnalyticsRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (!notificationScheduler.hasNotificationPermission()) return Result.success()

        val today = Date()
        val currentMonth = today.toMonthAndYearKey()
        val currentYear = today.toYear()
        val alreadySent = feedbackRepository.getSentBudgetAlerts().first()

        getBudgetsUseCase.invoke().first()
            .filter { budget ->
                when (budget.periodType) {
                    BudgetPeriod.MONTHLY -> budget.selectedMonth == currentMonth
                    BudgetPeriod.YEARLY -> budget.selectedMonth == currentYear
                }
            }
            .forEach { budget ->
                val threshold = when {
                    budget.percent >= OVER_PERCENT -> OVER_PERCENT
                    budget.percent >= WARNING_PERCENT -> WARNING_PERCENT
                    else -> return@forEach
                }
                val key = "${budget.id}:${budget.selectedMonth}:$threshold"
                if (key in alreadySent) return@forEach

                val spent = budget.transactionAmount.amountString.orEmpty()
                val limit = budget.amount.amountString.orEmpty()
                val (title, body) = if (threshold == OVER_PERCENT) {
                    context.getString(R.string.budget_alert_over_title, budget.name) to
                        context.getString(R.string.budget_alert_over_body, spent, limit)
                } else {
                    context.getString(R.string.budget_alert_warning_title, budget.name) to
                        context.getString(R.string.budget_alert_warning_body, spent, limit)
                }

                notificationScheduler.showInsightNotification(
                    notificationId = NotificationId.BUDGET_ALERT_BASE + abs(budget.id.hashCode() % 10_000),
                    title = title,
                    content = body,
                )
                feedbackRepository.markBudgetAlertSent(key)
                // Once over budget, the 80% alert is moot; mark it too so it never shows later.
                if (threshold == OVER_PERCENT) {
                    feedbackRepository.markBudgetAlertSent("${budget.id}:${budget.selectedMonth}:$WARNING_PERCENT")
                }
                analyticsRepository.logEvent(
                    AnalyticsEvents.BUDGET_ALERT_SHOWN,
                    mapOf(AnalyticsParams.THRESHOLD to threshold.toString()),
                )
            }
        return Result.success()
    }

    private companion object {
        const val WARNING_PERCENT = 80
        const val OVER_PERCENT = 100
    }
}
