package com.naveenapps.expensemanager.feature.category.di

import com.naveenapps.expensemanager.feature.category.create.CategoryCreateViewModel
import com.naveenapps.expensemanager.feature.category.details.CategoryDetailViewModel
import com.naveenapps.expensemanager.feature.category.list.CategoryListViewModel
import com.naveenapps.expensemanager.feature.category.selection.CategorySelectionViewModel
import com.naveenapps.expensemanager.feature.category.transaction.CategoryTransactionListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val CategoryViewModelModule = module {
    viewModel {
        CategorySelectionViewModel(
            getCategoriesUseCase = get()
        )
    }
    viewModel {
        CategoryCreateViewModel(
            savedStateHandle = get(),
            findCategoryByIdUseCase = get(),
            addCategoryUseCase = get(),
            updateCategoryUseCase = get(),
            deleteCategoryUseCase = get(),
            appComposeNavigator = get()
        )
    }
    viewModel {
        CategoryListViewModel(
            getAllCategoryUseCase = get(),
            appComposeNavigator = get()
        )
    }
    viewModel {
        CategoryDetailViewModel(
            savedStateHandle = get(),
            getCurrencyUseCase = get(),
            findCategoryByIdFlowUseCase = get(),
            getTransactionWithFilterUseCase = get(),
            getFormattedAmountUseCase = get(),
            appComposeNavigator = get()
        )
    }
    viewModel {
        CategoryTransactionListViewModel(
            getTransactionGroupByCategoryUseCase = get(),
            appComposeNavigator = get(),
        )
    }
}

