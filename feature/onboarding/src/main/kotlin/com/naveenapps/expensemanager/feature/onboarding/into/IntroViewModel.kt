package com.naveenapps.expensemanager.feature.onboarding.into

import androidx.lifecycle.ViewModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository

class IntroViewModel(
    private val appComposeNavigator: AppComposeNavigator,
    private val analyticsRepository: AnalyticsRepository,
) : ViewModel() {

    fun navigate() {
        analyticsRepository.logEvent(AnalyticsEvents.INTRO_GET_STARTED, emptyMap())
        appComposeNavigator.navigate(ExpenseManagerScreens.Onboarding)
    }
}
