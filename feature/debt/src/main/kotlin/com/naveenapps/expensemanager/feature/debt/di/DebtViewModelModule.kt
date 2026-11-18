package com.naveenapps.expensemanager.feature.debt.di

import com.naveenapps.expensemanager.feature.debt.entry.DebtEntryViewModel
import com.naveenapps.expensemanager.feature.debt.list.DebtListViewModel
import com.naveenapps.expensemanager.feature.debt.person.DebtPersonViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val DebtViewModelModule = module {
    viewModel {
        DebtListViewModel(
            getDebtBalancesUseCase = get(),
            getCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            appComposeNavigator = get(),
        )
    }
    viewModel {
        DebtPersonViewModel(
            savedStateHandle = get(),
            debtRepository = get(),
            getAllAccountsUseCase = get(),
            getCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            appComposeNavigator = get(),
            analyticsRepository = get(),
        )
    }
    viewModel {
        DebtEntryViewModel(
            savedStateHandle = get(),
            debtRepository = get(),
            saveDebtEntryUseCase = get(),
            getAllAccountsUseCase = get(),
            getCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            numberFormatRepository = get(),
            settingsRepository = get(),
            appComposeNavigator = get(),
            analyticsRepository = get(),
        )
    }
}
