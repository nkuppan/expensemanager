package com.nkuppan.expensemanager.data.startup

import android.content.Context
import androidx.startup.Initializer
import com.nkuppan.expensemanager.data.usecase.settings.theme.ApplyThemeUseCase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("unused")
class ThemeInitializer : Initializer<Unit> {

    @Inject
    lateinit var applyThemeUseCase: ApplyThemeUseCase

    @OptIn(DelicateCoroutinesApi::class)
    override fun create(context: Context) {

        InitializerEntryPoint.resolve(context).inject(this)

        GlobalScope.launch {
            applyThemeUseCase.invoke()
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(DependencyGraphInitializer::class.java)
    }
}