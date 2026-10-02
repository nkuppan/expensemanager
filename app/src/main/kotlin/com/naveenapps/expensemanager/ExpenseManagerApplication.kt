package com.naveenapps.expensemanager

import android.app.Application
import androidx.work.Configuration
import org.koin.androidx.workmanager.factory.KoinWorkerFactory

/**
 * WorkManager is initialised ON DEMAND (first WorkManager.getInstance call) via
 * [Configuration.Provider], instead of eagerly from the androidx.startup InitializationProvider.
 *
 * Why: WorkManager 2.10+ calls JobScheduler.forNamespace() when SDK_INT >= 34. Some devices
 * (modified ROMs, some Play pre-launch test images) report API 34 on an older framework that
 * lacks that method, so eager init threw NoSuchMethodError inside the ContentProvider and
 * crashed the app before any of our code ran. On demand, the failure happens inside
 * NotificationScheduler, which catches it: those devices just lose reminders, not the app.
 */
class ExpenseManagerApplication :
    Application(),
    Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            // Koin is started by KoinInitializer (androidx.startup) before any WorkManager use.
            .setWorkerFactory(KoinWorkerFactory())
            .build()
}
