package com.naveenapps.expensemanager.di

import com.naveenapps.expensemanager.analytics.UserPropertiesTracker
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val AnalyticsModule = module {
    single {
        UserPropertiesTracker(
            context = androidContext(),
            analyticsRepository = get(),
            feedbackRepository = get(),
            reminderTimeRepository = get(),
            budgetRepository = get(),
            accountRepository = get(),
            themeRepository = get(),
            currencyRepository = get(),
        )
    }
}
