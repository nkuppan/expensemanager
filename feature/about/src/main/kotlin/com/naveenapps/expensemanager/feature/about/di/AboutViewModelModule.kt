package com.naveenapps.expensemanager.feature.about.di

import com.naveenapps.expensemanager.feature.about.AboutUsViewModel
import com.naveenapps.expensemanager.feature.about.licenses.OpenSourceLicenseDetailViewModel
import com.naveenapps.expensemanager.feature.about.licenses.OpenSourceLicensesRepository
import com.naveenapps.expensemanager.feature.about.licenses.OpenSourceLicensesViewModel
import com.naveenapps.expensemanager.feature.about.licenses.RawResourceLicensesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val AboutViewModelModule = module {
    // One instance, so the licence files are read once and shared by the list and detail screens.
    single<OpenSourceLicensesRepository> {
        RawResourceLicensesRepository(context = androidContext(), dispatchers = get())
    }
    viewModel {
        AboutUsViewModel(
            appComposeNavigator = get(),
            devicePropertyRepository = get(),
        )
    }
    viewModel {
        OpenSourceLicensesViewModel(
            repository = get(),
            appComposeNavigator = get(),
        )
    }
    viewModel {
        OpenSourceLicenseDetailViewModel(
            savedStateHandle = get(),
            repository = get(),
            appComposeNavigator = get(),
        )
    }
}
