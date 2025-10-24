package com.naveenapps.expensemanager.feature.budget.di

import com.naveenapps.expensemanager.feature.budget.create.BudgetCreateViewModel
import com.naveenapps.expensemanager.feature.budget.details.BudgetDetailViewModel
import com.naveenapps.expensemanager.feature.budget.list.BudgetListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val BudgetViewModelModule = module {
    viewModel {
        BudgetCreateViewModel(
            savedStateHandle = get(),
            getCurrencyUseCase = get(),
            getDefaultCurrencyUseCase = get(),
            findBudgetByIdUseCase = get(),
            findAccountByIdUseCase = get(),
            findCategoryByIdUseCase = get(),
            addBudgetUseCase = get(),
            updateBudgetUseCase = get(),
            deleteBudgetUseCase = get(),
            appComposeNavigator = get()
        )
    }
    viewModel {
        BudgetDetailViewModel(
            savedStateHandle = get(),
            getBudgetDetailUseCase = get(),
            appComposeNavigator = get()
        )
    }
    viewModel {
        BudgetListViewModel(
            getBudgetsUseCase = get(),
            appComposeNavigator = get()
        )
    }
}

