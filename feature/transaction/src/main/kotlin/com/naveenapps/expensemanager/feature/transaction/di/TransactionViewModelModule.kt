package com.naveenapps.expensemanager.feature.transaction.di

import com.naveenapps.expensemanager.feature.transaction.create.TransactionCreateViewModel
import com.naveenapps.expensemanager.feature.transaction.list.TransactionListViewModel
import com.naveenapps.expensemanager.feature.transaction.numberpad.NumberPadViewModel
import com.naveenapps.expensemanager.feature.transaction.recurring.RecurringTransactionsViewModel
import com.naveenapps.expensemanager.feature.transaction.recurring.edit.RecurringTransactionEditViewModel
import com.naveenapps.expensemanager.feature.transaction.search.CategoryDisplayNames
import com.naveenapps.expensemanager.feature.transaction.search.LocalizedCategoryDisplayNames
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val TransactionViewModelModule = module {
    // Category names as shown in the app's language, so search matches translated names.
    factory<CategoryDisplayNames> { LocalizedCategoryDisplayNames(androidContext(), get()) }
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
            findRecurringTransactionByIdUseCase = get(),
            getFrequentTransactionsUseCase = get(),
            planRecurringTransactionUseCase = get(),
        )
    }
    viewModel {
        TransactionListViewModel(
            getCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            getTransactionWithFilterUseCase = get(),
            appCoroutineDispatchers = get(),
            appComposeNavigator = get(),
            deleteTransactionUseCase = get(),
            addTransactionUseCase = get(),
            imageStorageRepository = get(),
            categoryDisplayNames = get(),
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
            categoryDisplayNames = get(),
        )
    }
    viewModel {
        RecurringTransactionEditViewModel(
            savedStateHandle = get(),
            findRecurringTransactionByIdUseCase = get(),
            updateRecurringTransactionUseCase = get(),
            deleteRecurringTransactionUseCase = get(),
            getCurrencyUseCase = get(),
            getAllAccountsUseCase = get(),
            getAllCategoryUseCase = get(),
            getFormattedAmountUseCase = get(),
            numberFormatRepository = get(),
            appComposeNavigator = get(),
            analyticsRepository = get(),
        )
    }
}
