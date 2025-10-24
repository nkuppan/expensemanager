package com.naveenapps.expensemanager.initializer

import android.content.Context
import androidx.startup.Initializer
import androidx.work.Configuration

class WorkManagerInitializer : Initializer<Configuration> {

    override fun create(context: Context): Configuration {
        // Ensure Koin is already started
        val workerFactory = org.koin.androidx.workmanager.factory.KoinWorkerFactory()

        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    // Depend on KoinInitializer so it starts first
    override fun dependencies(): List<Class<out Initializer<*>>> =
        listOf(KoinInitializer::class.java)
}