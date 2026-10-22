package com.naveenapps.expensemanager.feature.settings.di

import com.naveenapps.expensemanager.feature.settings.SettingsViewModel
import com.naveenapps.expensemanager.feature.settings.advanced.AdvancedSettingsViewModel
import com.naveenapps.expensemanager.feature.settings.autobackup.AutoBackupViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val SettingsViewModelModule = module {
    viewModel {
        SettingsViewModel(
            getDefaultCurrencyUseCase = get(),
            getCurrencyUseCase = get(),
            getCurrentThemeUseCase = get(),
            getCurrentLocaleUseCase = get(),
            getAllAccountsUseCase = get(),
            getAllCategoryUseCase = get(),
            settingsRepository = get(),
            appComposeNavigator = get(),
            analyticsRepository = get(),
            backupRepository = get(),
            // Only bound in debug builds.
            demoDataRepository = getOrNull(),
        )
    }
    viewModel {
        AutoBackupViewModel(
            autoBackupRepository = get(),
            autoBackupScheduler = get(),
            appComposeNavigator = get(),
            analyticsRepository = get(),
        )
    }
    viewModel {
        AdvancedSettingsViewModel(
            appComposeNavigator = get(),
            settingsRepository = get(),
        )
    }
}
