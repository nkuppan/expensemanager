package com.naveenapps.expensemanager.core.data.di

import com.naveenapps.expensemanager.core.data.repository.AccountRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.BudgetRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.CategoryRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.CurrencyRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.DateRangeFilterRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.ExportRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.ReminderTimeRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.SettingsRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.ThemeRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.TransactionRepositoryImpl
import com.naveenapps.expensemanager.core.domain.repository.AccountRepository
import com.naveenapps.expensemanager.core.domain.repository.BudgetRepository
import com.naveenapps.expensemanager.core.domain.repository.CategoryRepository
import com.naveenapps.expensemanager.core.domain.repository.CurrencyRepository
import com.naveenapps.expensemanager.core.domain.repository.DateRangeFilterRepository
import com.naveenapps.expensemanager.core.domain.repository.ExportRepository
import com.naveenapps.expensemanager.core.domain.repository.ReminderTimeRepository
import com.naveenapps.expensemanager.core.domain.repository.SettingsRepository
import com.naveenapps.expensemanager.core.domain.repository.ThemeRepository
import com.naveenapps.expensemanager.core.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Dependence injection repository module. This will create repository and use cases related object
 * and it's relations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun provideCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl,
    ): CategoryRepository

    @Binds
    abstract fun provideAccountRepository(
        accountRepositoryImpl: AccountRepositoryImpl,
    ): AccountRepository

    @Binds
    abstract fun provideTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl,
    ): TransactionRepository

    @Binds
    abstract fun provideThemeRepository(
        themeRepositoryImpl: ThemeRepositoryImpl,
    ): ThemeRepository

    @Binds
    abstract fun provideCurrencyRepository(
        currencyRepositoryImpl: CurrencyRepositoryImpl,
    ): CurrencyRepository

    @Binds
    abstract fun provideReminderTimeRepository(
        reminderTimeRepositoryImpl: ReminderTimeRepositoryImpl,
    ): ReminderTimeRepository

    @Binds
    abstract fun provideSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl,
    ): SettingsRepository

    @Binds
    abstract fun provideBudgetRepository(
        budgetRepositoryImpl: BudgetRepositoryImpl,
    ): BudgetRepository

    @Binds
    abstract fun provideExportRepository(
        exportRepositoryImpl: ExportRepositoryImpl,
    ): ExportRepository

    @Binds
    abstract fun provideDateRangeFilterRepository(
        dateRangeFilterRepositoryImpl: DateRangeFilterRepositoryImpl,
    ): DateRangeFilterRepository
}