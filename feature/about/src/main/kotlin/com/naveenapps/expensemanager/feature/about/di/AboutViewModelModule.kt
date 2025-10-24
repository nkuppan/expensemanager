package com.naveenapps.expensemanager.feature.about.di

import com.naveenapps.expensemanager.feature.about.AboutUsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val AboutViewModelModule = module {
    viewModel {
        AboutUsViewModel(
            appComposeNavigator = get()
        )
    }
}

