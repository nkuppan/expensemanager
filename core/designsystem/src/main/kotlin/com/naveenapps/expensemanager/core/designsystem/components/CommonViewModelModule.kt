package com.naveenapps.expensemanager.core.designsystem.components

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val CommonViewModelModule = module {
    viewModel {
        IconSelectionViewModel()
    }
}