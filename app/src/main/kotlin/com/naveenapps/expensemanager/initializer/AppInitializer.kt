package com.naveenapps.expensemanager.initializer

import android.content.Context
import androidx.startup.Initializer
import com.naveenapps.expensemanager.core.domain.usecase.settings.theme.ApplyThemeUseCase
import com.naveenapps.expensemanager.core.notification.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppInitializer : Initializer<Unit> {

    @Inject
    lateinit var applyThemeUseCase: ApplyThemeUseCase

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    override fun create(context: Context) {
        InitializerEntryPoint.resolve(context).inject(this)

        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            applyThemeUseCase.invoke()
            notificationScheduler.checkAndRestartReminder()
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(DependencyGraphInitializer::class.java)
    }
}
