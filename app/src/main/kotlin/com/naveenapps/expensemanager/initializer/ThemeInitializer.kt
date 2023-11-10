package com.naveenapps.expensemanager.initializer

import android.content.Context
import androidx.startup.Initializer
import com.naveenapps.expensemanager.core.domain.usecase.settings.theme.ApplyThemeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

class ThemeInitializer : Initializer<Unit> {

    @Inject
    lateinit var applyThemeUseCase: ApplyThemeUseCase

    override fun create(context: Context) {

        InitializerEntryPoint.resolve(context).inject(this)

        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            applyThemeUseCase.invoke()
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(DependencyGraphInitializer::class.java)
    }
}