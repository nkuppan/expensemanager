package com.naveenapps.expensemanager.initializer

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import com.naveenapps.expensemanager.core.domain.usecase.recurring.ProcessRecurringTransactionsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.locale.ApplyLocaleUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.theme.ApplyThemeUseCase
import com.naveenapps.expensemanager.core.notification.NotificationScheduler
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

class AppInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val applyThemeUseCase: ApplyThemeUseCase = GlobalContext.get().get()
        val applyLocaleUseCase: ApplyLocaleUseCase = GlobalContext.get().get()
        val notificationScheduler: NotificationScheduler = GlobalContext.get().get()
        val processRecurringTransactions: ProcessRecurringTransactionsUseCase = GlobalContext.get().get()
        val analyticsRepository: AnalyticsRepository = GlobalContext.get().get()

        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            applyThemeUseCase.invoke()
            applyLocaleUseCase.invoke()
            notificationScheduler.checkAndRestartReminder()
            notificationScheduler.scheduleWeeklySummary()
            notificationScheduler.scheduleRecurringTransactions()
        }
        // Off the main thread: due recurring transactions are in place by the time Home loads
        // its data (Room flows pick them up either way).
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val created = runCatching { processRecurringTransactions.invoke() }
                .onFailure { Log.w("AppInitializer", "Recurring transactions failed", it) }
                .getOrDefault(0)
            if (created > 0) {
                notificationScheduler.checkBudgetsSoon()
                analyticsRepository.logEvent(
                    AnalyticsEvents.RECURRING_GENERATED,
                    mapOf(AnalyticsParams.COUNT to created.toString(), AnalyticsParams.SOURCE to "app_start"),
                )
            }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(
        KoinInitializer::class.java,
    )
}
