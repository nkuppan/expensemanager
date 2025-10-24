package com.naveenapps.expensemanager.feature.analysis.di

import com.naveenapps.expensemanager.feature.analysis.AnalysisScreenViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val AnalysisViewModelModule = module {
    viewModel {
        AnalysisScreenViewModel(
            getCurrentThemeUseCase = get(),
            getChartDataUseCase = get(),
            getAverageDataUseCase = get(),
            getAmountStateUseCase = get()
        )
    }
}

