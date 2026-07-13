package com.naveenapps.expensemanager.feature.language.di

import com.naveenapps.expensemanager.feature.language.LanguageViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val LanguageViewModelModule = module {
    viewModel {
        LanguageViewModel(
            getCurrentLocaleUseCase = get(),
            getLocalesUseCase = get(),
            saveLocaleUseCase = get()
        )
    }
}
