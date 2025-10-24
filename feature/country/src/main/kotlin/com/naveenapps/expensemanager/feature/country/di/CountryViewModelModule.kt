package com.naveenapps.expensemanager.feature.country.di

import com.naveenapps.expensemanager.feature.country.CountryListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val CountryViewModelModule = module {
    viewModel {
        CountryListViewModel(getCountiesUseCase = get())
    }
}