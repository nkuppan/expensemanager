package com.naveenapps.expensemanager.feature.dashboard.di

import com.naveenapps.expensemanager.feature.dashboard.DashboardViewModel
import com.naveenapps.expensemanager.feature.dashboard.customise.DashboardReOrderViewModel
import com.naveenapps.expensemanager.feature.dashboard.notifications.NotificationCentreViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val DashboardViewModelModule = module {
    viewModel {
        DashboardViewModel(
            getTransactionWithFilterUseCase = get(),
            getCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            getAllAccountsUseCase = get(),
            getTransactionGroupByCategoryUseCase = get(),
            getBudgetsUseCase = get(),
            appCoroutineDispatchers = get(),
            appComposeNavigator = get(),
            getDateRangeUseCase = get(),
            settingsRepository = get(),
            feedbackRepository = get(),
            analyticsRepository = get(),
            autoBackupRepository = get(),
            getUpcomingRecurringUseCase = get(),
            reminderTimeRepository = get(),
            updateReminderStatusUseCase = get(),
            notificationScheduler = get(),
        )
    }
    viewModel {
        NotificationCentreViewModel(
            feedbackRepository = get(),
            appComposeNavigator = get(),
        )
    }
    viewModel {
        DashboardReOrderViewModel(
            settingsRepository = get(),
            appComposeNavigator = get(),
            analyticsRepository = get(),
        )
    }
}
