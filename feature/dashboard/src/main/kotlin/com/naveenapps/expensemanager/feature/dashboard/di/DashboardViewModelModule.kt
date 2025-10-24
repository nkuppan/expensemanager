package com.naveenapps.expensemanager.feature.dashboard.di

import com.naveenapps.expensemanager.feature.dashboard.DashboardViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val DashboardViewModelModule = module {
    viewModel {
        DashboardViewModel(
            getTransactionWithFilterUseCase = get(),
            getCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            getAllAccountsUseCase = get(),
            getTransactionGroupByCategoryUseCase = get(),
            getBudgetsUseCase = get(),
            appCoroutineDispatchers = get(),
            appComposeNavigator = get()
        )
    }
}

