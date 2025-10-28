package com.naveenapps.expensemanager.feature.reminder.di

import com.naveenapps.expensemanager.feature.reminder.ReminderViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val ReminderViewModelModule = module {
    viewModel {
        ReminderViewModel(
            getReminderTimeUseCase = get(),
            getReminderStatusUseCase = get(),
            updateReminderStatusUseCase = get(),
            notificationScheduler = get(),
            appComposeNavigator = get(),
            saveReminderTimeUseCase = get(),
            versionCheckerRepository = get(),
        )
    }
}

