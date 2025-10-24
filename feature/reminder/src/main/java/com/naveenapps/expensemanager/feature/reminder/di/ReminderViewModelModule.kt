package com.naveenapps.expensemanager.feature.reminder.di

import com.naveenapps.expensemanager.feature.reminder.ReminderTimePickerViewModel
import com.naveenapps.expensemanager.feature.reminder.ReminderViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val ReminderViewModelModule = module {
    viewModel {
        ReminderTimePickerViewModel(
            getReminderTimeUseCase = get(),
            saveReminderTimeUseCase = get()
        )
    }
    viewModel {
        ReminderViewModel(
            getReminderTimeUseCase = get(),
            getReminderStatusUseCase = get(),
            updateReminderStatusUseCase = get(),
            notificationScheduler = get(),
            appComposeNavigator = get()
        )
    }
}

