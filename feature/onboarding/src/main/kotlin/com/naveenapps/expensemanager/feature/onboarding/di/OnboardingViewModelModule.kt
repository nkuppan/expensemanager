package com.naveenapps.expensemanager.feature.onboarding.di

import com.naveenapps.expensemanager.feature.onboarding.OnboardingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val OnboardingViewModelModule = module {
    viewModel {
        OnboardingViewModel(
            getOnboardingStatusUseCase = get(),
            getAllAccountsUseCase = get(),
            getDefaultCurrencyUseCase = get(),
            preselectCountryCurrencyUseCase = get(),
            getCurrencyUseCase = get(),
            saveCurrencyUseCase = get(),
            setOnboardingStatusUseCase = get(),
            getFormattedAmountUseCase = get(),
            composeNavigator = get(),
            analyticsRepository = get(),
        )
    }
}
