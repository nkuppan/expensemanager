package com.naveenapps.expensemanager.feature.account.di

import com.naveenapps.expensemanager.feature.account.create.AccountCreateViewModel
import com.naveenapps.expensemanager.feature.account.list.AccountListViewModel
import com.naveenapps.expensemanager.feature.account.reorder.AccountReOrderViewModel
import com.naveenapps.expensemanager.feature.account.selection.AccountSelectionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val AccountViewModelModule = module {
    viewModel {
        AccountCreateViewModel(
            savedStateHandle = get(),
            getCurrencyUseCase = get(),
            getDefaultCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            findAccountByIdUseCase = get(),
            addAccountUseCase = get(),
            updateAccountUseCase = get(),
            deleteAccountUseCase = get(),
            archiveAccountUseCase = get(),
            accountRepository = get(),
            getCountriesUseCase = get(),
            imageStorageRepository = get(),
            composeNavigator = get(),
            numberFormatRepository = get(),
            exchangeRateRepository = get(),
            getAccountDeleteImpactUseCase = get(),
            analyticsRepository = get(),
        )
    }
    viewModel {
        AccountSelectionViewModel(
            getAccountUiModelUseCase = get(),
            getAllAccountsUseCase = get(),
        )
    }
    viewModel {
        AccountListViewModel(
            getAllAccountsUseCase = get(),
            getCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            convertAmountUseCase = get(),
            formatAmountInCurrencyUseCase = get(),
            archiveAccountUseCase = get(),
            appComposeNavigator = get(),
        )
    }
    viewModel {
        AccountReOrderViewModel(
            getAllAccountsUseCase = get(),
            updateAllAccountUseCase = get(),
            appComposeNavigator = get(),
        )
    }
}
