package com.naveenapps.expensemanager.feature.onboarding.into

import androidx.lifecycle.ViewModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class IntroViewModel @Inject constructor(
    private val appComposeNavigator: AppComposeNavigator
) : ViewModel() {

    fun navigate() {
        appComposeNavigator.navigate(ExpenseManagerScreens.Onboarding)
    }
}