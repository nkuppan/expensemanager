package com.nkuppan.expensemanager

import android.app.Application
import com.nkuppan.expensemanager.domain.usecase.settings.theme.ApplyThemeUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ExpenseManagerApplication : Application() {

    @Inject
    lateinit var applyThemeUseCase: ApplyThemeUseCase

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        GlobalScope.launch(Dispatchers.Main) {
            applyThemeUseCase.invoke()
        }
    }
}