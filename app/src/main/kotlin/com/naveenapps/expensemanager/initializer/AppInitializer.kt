package com.naveenapps.expensemanager.initializer

import android.content.Context
import androidx.startup.Initializer
import com.naveenapps.expensemanager.core.domain.usecase.settings.theme.ApplyThemeUseCase
import com.naveenapps.expensemanager.core.notification.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

class AppInitializer : Initializer<Unit> {

    override fun create(context: Context) {

        val applyThemeUseCase: ApplyThemeUseCase = GlobalContext.get().get()
        val notificationScheduler: NotificationScheduler = GlobalContext.get().get()

        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            applyThemeUseCase.invoke()
            notificationScheduler.checkAndRestartReminder()
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(
            KoinInitializer::class.java,
            WorkManagerInitializer::class.java
        )
    }
}
