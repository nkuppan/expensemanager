package com.naveenapps.expensemanager.feature.transaction.di

import com.naveenapps.expensemanager.feature.transaction.create.TransactionCreateViewModel
import com.naveenapps.expensemanager.feature.transaction.list.TransactionListViewModel
import com.naveenapps.expensemanager.feature.transaction.numberpad.NumberPadViewModel
import com.naveenapps.expensemanager.feature.transaction.recurring.RecurringTransactionsViewModel
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
            imageStorageRepository = get(),
            appComposeNavigator = get(),
            numberFormatRepository = get(),
            feedbackRepository = get(),
            analyticsRepository = get(),
            budgetAlertTrigger = get(),
            createRecurringTransactionUseCase = get(),
        )
    }
    viewModel {
        TransactionListViewModel(
            getCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            getTransactionWithFilterUseCase = get(),
            appCoroutineDispatchers = get(),
            appComposeNavigator = get(),
        )
    }
    viewModel { NumberPadViewModel() }
    viewModel {
        RecurringTransactionsViewModel(
            getRecurringTransactionsUseCase = get(),
            getCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            deleteRecurringTransactionUseCase = get(),
            appComposeNavigator = get(),
            analyticsRepository = get(),
        )
    }
}
