package com.nkuppan.expensemanager

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.nkuppan.expensemanager.domain.usecase.settings.theme.ApplyThemeUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import javax.inject.Inject

@HiltAndroidApp
class ExpenseManagerApplication : Application() {

    @Inject
    lateinit var applyThemeUseCase: ApplyThemeUseCase

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        /*GlobalScope.launch(Dispatchers.Main) {
            applyThemeUseCase.invoke()
        }*/
    }
}