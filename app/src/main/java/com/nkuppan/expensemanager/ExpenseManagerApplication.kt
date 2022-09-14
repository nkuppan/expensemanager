package com.nkuppan.expensemanager

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExpenseManagerApplication : Application()

/*

import javax.inject.Inject

@HiltAndroidApp
class ExpenseManagerApplication : Application() {
    @Inject
    lateinit var applyThemeUseCase: ApplyThemeUseCase
}*/
