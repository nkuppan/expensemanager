package com.naveenapps.expensemanager.di

import com.naveenapps.expensemanager.MainViewModel
import com.naveenapps.expensemanager.core.designsystem.components.CommonViewModelModule
import com.naveenapps.expensemanager.feature.about.di.AboutViewModelModule
import com.naveenapps.expensemanager.feature.account.di.AccountViewModelModule
import com.naveenapps.expensemanager.feature.analysis.di.AnalysisViewModelModule
import com.naveenapps.expensemanager.feature.budget.di.BudgetViewModelModule
import com.naveenapps.expensemanager.feature.category.di.CategoryViewModelModule
import com.naveenapps.expensemanager.feature.country.di.CountryViewModelModule
import com.naveenapps.expensemanager.feature.currency.di.CurrencyViewModelModule
import com.naveenapps.expensemanager.feature.dashboard.di.DashboardViewModelModule
import com.naveenapps.expensemanager.feature.export.di.ExportViewModelModule
import com.naveenapps.expensemanager.feature.filter.di.FilterViewModelModule
import com.naveenapps.expensemanager.feature.onboarding.di.OnboardingViewModelModule
import com.naveenapps.expensemanager.feature.reminder.di.ReminderViewModelModule
import com.naveenapps.expensemanager.feature.settings.di.SettingsViewModelModule
import com.naveenapps.expensemanager.feature.theme.di.ThemeViewModelModule
import com.naveenapps.expensemanager.feature.transaction.di.TransactionViewModelModule
import com.naveenapps.expensemanager.ui.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val MainViewModelModule = module {
    viewModel {
        MainViewModel(
            getCurrentThemeUseCase = get(),
            getOnboardingStatusUseCase = get()
        )
    }
    viewModel {
        HomeViewModel(
            updateReminderStatusUseCase = get(),
            notificationScheduler = get()
        )
    }
}

val ViewModelModule = module {
    includes(
        MainViewModelModule,
        AccountViewModelModule,
        AnalysisViewModelModule,
        BudgetViewModelModule,
        CategoryViewModelModule,
        DashboardViewModelModule,
        TransactionViewModelModule,
        OnboardingViewModelModule,
        SettingsViewModelModule,
        ThemeViewModelModule,
        ExportViewModelModule,
        ReminderViewModelModule,
        CurrencyViewModelModule,
        AboutViewModelModule,
        FilterViewModelModule,
        CountryViewModelModule,
        CommonViewModelModule,
    )
}
