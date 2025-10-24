package com.naveenapps.expensemanager.core.notification

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val NotificationModule = module {
    single<NotificationScheduler> {
        NotificationScheduler(
            context = androidContext(),
            reminderTimeRepository = get()
        )
    }
    worker {
        NotificationWorker(
            context = androidContext(),
            workerParams = get(),
            notificationScheduler = get()
        )
    }
}