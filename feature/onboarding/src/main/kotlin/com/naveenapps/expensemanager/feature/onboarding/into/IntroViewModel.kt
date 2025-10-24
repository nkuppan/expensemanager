package com.naveenapps.expensemanager.feature.onboarding.into

import androidx.lifecycle.ViewModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens


class IntroViewModel(
    private val appComposeNavigator: AppComposeNavigator
) : ViewModel() {

    fun navigate() {
        appComposeNavigator.navigate(ExpenseManagerScreens.Onboarding)
    }
}