package com.naveenapps.expensemanager.core.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.naveenapps.expensemanager.core.domain.usecase.debt.GetDebtBalancesUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.DebtPersonBalance
import com.naveenapps.expensemanager.core.model.milliToDouble
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.ReminderTimeRepository
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs
import kotlinx.coroutines.flow.first

/**
 * Runs each morning at [DEBT_DUE_HOUR]: when a debt with an open balance is due today, shows
 * one notification ("Due today: Ravi", "Ravi owes you ₹1,500"), or a list when several are.
 * Uses the "Upcoming payments" channel and its on/off switch. Then schedules tomorrow's check.
 * The amounts appear only in the notification on the phone; analytics gets just a count.
 */
class DebtDueWorker(
    private val context: Context,
    workerParams: WorkerParameters,
    private val getDebtBalancesUseCase: GetDebtBalancesUseCase,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val reminderTimeRepository: ReminderTimeRepository,
    private val notificationScheduler: NotificationScheduler,
    private val analyticsRepository: AnalyticsRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            if (reminderTimeRepository.isUpcomingAlertOn().first()) {
                val zone = ZoneId.systemDefault()
                val today = LocalDate.now(zone)
                val due = getDebtBalancesUseCase().first().filter { balance ->
                    !balance.isSettled &&
                        balance.dueDate?.toInstant()?.atZone(zone)?.toLocalDate() == today
                }
                if (due.isNotEmpty()) {
                    notifyAbout(due)
                    analyticsRepository.logEvent(
                        AnalyticsEvents.DEBT_DUE_REMINDER_SHOWN,
                        mapOf(AnalyticsParams.COUNT to due.size.toString()),
                    )
                }
            }
        } finally {
            notificationScheduler.scheduleDebtDueCheck()
        }
        return Result.success()
    }

    private suspend fun notifyAbout(due: List<DebtPersonBalance>) {
        val currency = getCurrencyUseCase.invoke().first()
        fun lineFor(balance: DebtPersonBalance): String {
            val amount = getFormattedAmountUseCase(abs(balance.balanceMilli).milliToDouble(), currency)
                .amountString.orEmpty()
            return if (balance.owesUser) {
                context.getString(R.string.debt_due_owes_you, balance.person.name, amount)
            } else {
                context.getString(R.string.debt_due_you_owe, balance.person.name, amount)
            }
        }
        val single = due.singleOrNull()
        notificationScheduler.showUpcomingRecurringNotification(
            title = if (single != null) {
                context.getString(R.string.debt_due_title_one, single.person.name)
            } else {
                context.getString(R.string.debt_due_title_many, due.size)
            },
            lines = due.map(::lineFor),
            summary = "",
        )
    }
}
