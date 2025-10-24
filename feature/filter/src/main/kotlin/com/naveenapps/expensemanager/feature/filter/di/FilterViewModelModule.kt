package com.naveenapps.expensemanager.feature.filter.di

import com.naveenapps.expensemanager.feature.filter.FilterViewModel
import com.naveenapps.expensemanager.feature.filter.datefilter.DateFilterViewModel
import com.naveenapps.expensemanager.feature.filter.type.FilterTypeSelectionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val FilterViewModelModule = module{
    viewModel {
        FilterViewModel(
            getSelectedTransactionTypesUseCase = get(),
            getSelectedAccountUseCase = get(),
            getSelectedCategoriesUseCase = get(),
            getDateRangeUseCase = get(),
            moveDateRangeBackwardUseCase = get(),
            moveDateRangeForwardUseCase = get(),
            updateSelectedTransactionTypesUseCase = get(),
            updateSelectedCategoryUseCase = get(),
            updateSelectedAccountUseCase = get()
        )
    }
    viewModel {
        DateFilterViewModel(
            getDateRangeUseCase = get(),
            getAllDateRangeUseCase = get(),
            saveDateRangeUseCase = get()
        )
    }
    viewModel {
        FilterTypeSelectionViewModel(
            getSelectedTransactionTypesUseCase = get(),
            getSelectedAccountUseCase = get(),
            getSelectedCategoriesUseCase = get(),
            getAllAccountsUseCase = get(),
            getAllCategoryUseCase = get(),
            updateSelectedTransactionTypesUseCase = get(),
            updateSelectedCategoryUseCase = get(),
            updateSelectedAccountUseCase = get()
        )
    }
}