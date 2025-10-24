package com.naveenapps.expensemanager.core.domain.usecase.di

import com.naveenapps.expensemanager.core.domain.usecase.account.AccountUseCaseModule
import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUseCaseModule
import com.naveenapps.expensemanager.core.domain.usecase.category.CategoryUseCaseModule
import com.naveenapps.expensemanager.core.domain.usecase.country.CountryUseCaseModule
import com.naveenapps.expensemanager.core.domain.usecase.settings.SettingsUseCaseModule
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.FilterUseCaseModule
import com.naveenapps.expensemanager.core.domain.usecase.transaction.TransactionUseCaseModule
import org.koin.dsl.module

val UseCaseModule = module {
    includes(
        AccountUseCaseModule,
        BudgetUseCaseModule,
        CategoryUseCaseModule,
        CountryUseCaseModule,
        SettingsUseCaseModule,
        TransactionUseCaseModule,
        FilterUseCaseModule,
    )
}