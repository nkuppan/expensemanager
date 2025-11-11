package com.naveenapps.expensemanager.feature.transaction.di

import com.naveenapps.expensemanager.feature.transaction.create.TransactionCreateViewModel
import com.naveenapps.expensemanager.feature.transaction.list.TransactionListViewModel
import com.naveenapps.expensemanager.feature.transaction.numberpad.NumberPadViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val TransactionViewModelModule = module {
    viewModel {
        TransactionCreateViewModel(
            savedStateHandle = get(),
            getCurrencyUseCase = get(),
            getAllAccountsUseCase = get(),
            getAllCategoryUseCase = get(),
            getDefaultCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            findTransactionByIdUseCase = get(),
            addTransactionUseCase = get(),
            updateTransactionUseCase = get(),
            deleteTransactionUseCase = get(),
            settingsRepository = get(),
            appComposeNavigator = get(),
            numberFormatRepository = get(),
        )
    }
    viewModel {
        TransactionListViewModel(
            getCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            getTransactionWithFilterUseCase = get(),
            appCoroutineDispatchers = get(),
            appComposeNavigator = get()
        )
    }
    viewModel { NumberPadViewModel() }
}

