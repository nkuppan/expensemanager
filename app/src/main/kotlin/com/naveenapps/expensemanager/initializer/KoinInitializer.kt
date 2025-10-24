package com.naveenapps.expensemanager.initializer

import DatabaseModule
import DatastoreModule
import DispatcherModule
import android.content.Context
import androidx.startup.Initializer
import com.naveenapps.expensemanager.core.data.di.ActivityModule
import com.naveenapps.expensemanager.core.data.di.AppModule
import com.naveenapps.expensemanager.core.data.di.RepositoryModule
import com.naveenapps.expensemanager.core.domain.usecase.di.UseCaseModule
import com.naveenapps.expensemanager.core.navigation.NavigationModule
import com.naveenapps.expensemanager.core.notification.NotificationModule
import com.naveenapps.expensemanager.di.ViewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

class KoinInitializer : Initializer<KoinApplication> {
    override fun create(context: Context): KoinApplication {
        return startKoin {
            androidLogger()
            androidContext(context)
            workManagerFactory()
            modules(
                AppModule,
                ActivityModule,
                DispatcherModule,
                DatastoreModule,
                RepositoryModule,
                UseCaseModule,
                DatabaseModule,
                NavigationModule,
                ViewModelModule,
                NotificationModule
            )
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> {
        return emptyList()
    }
}