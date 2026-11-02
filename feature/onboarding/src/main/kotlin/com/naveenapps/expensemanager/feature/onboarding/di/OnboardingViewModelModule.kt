package com.naveenapps.expensemanager.feature.onboarding.di

import com.naveenapps.expensemanager.feature.onboarding.OnboardingViewModel
import com.naveenapps.expensemanager.feature.onboarding.into.IntroViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val OnboardingViewModelModule = module {
    viewModel {
        OnboardingViewModel(
            getOnboardingStatusUseCase = get(),
            getAllAccountsUseCase = get(),
            getDefaultCurrencyUseCase = get(),
            getCurrencyUseCase = get(),
            saveCurrencyUseCase = get(),
            addAccountUseCase = get(),
            deleteAccountUseCase = get(),
            setOnboardingStatusUseCase = get(),
            getAccountUiModelUseCase = get(),
            composeNavigator = get(),
            analyticsRepository = get(),
        )
    }
    viewModel {
        IntroViewModel(
            appComposeNavigator = get(),
            analyticsRepository = get(),
        )
    }
}
