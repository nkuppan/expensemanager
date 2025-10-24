package com.naveenapps.expensemanager.feature.settings.di

import com.naveenapps.expensemanager.feature.settings.SettingsViewModel
import com.naveenapps.expensemanager.feature.settings.advanced.AdvancedSettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val SettingsViewModelModule = module {
    viewModel {
        SettingsViewModel(
            getDefaultCurrencyUseCase = get(),
            getCurrencyUseCase = get(),
            getCurrentThemeUseCase = get(),
            appComposeNavigator = get()
        )
    }
    viewModel {
        AdvancedSettingsViewModel(
            getAllCategoryUseCase = get(),
            getAllAccountsUseCase = get(),
            settingsRepository = get(),
            appComposeNavigator = get()
        )
    }
}

