package com.naveenapps.expensemanager.core.notification

import com.naveenapps.expensemanager.core.repository.BudgetAlertTrigger
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val NotificationModule = module {
    single<NotificationScheduler> {
        NotificationScheduler(
            context = androidContext(),
            reminderTimeRepository = get(),
        )
    }
    // The transaction screen asks for budget checks through this interface (core:repository).
    single<BudgetAlertTrigger> { get<NotificationScheduler>() }
    worker {
        WeeklySummaryWorker(
            context = androidContext(),
            workerParams = get(),
            notificationScheduler = get(),
            transactionRepository = get(),
            getCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            analyticsRepository = get(),
        )
    }
    worker {
        BudgetAlertWorker(
            context = androidContext(),
            workerParams = get(),
            notificationScheduler = get(),
            getBudgetsUseCase = get(),
            feedbackRepository = get(),
            analyticsRepository = get(),
        )
    }
    worker {
        NotificationWorker(
            context = androidContext(),
            workerParams = get(),
            notificationScheduler = get(),
            feedbackRepository = get(),
            analyticsRepository = get(),
        )
    }
}
