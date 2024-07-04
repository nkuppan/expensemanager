package com.naveenapps.expensemanager.core.data.di

import com.naveenapps.expensemanager.core.data.repository.AccountRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.BudgetRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.CategoryRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.CountryRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.CurrencyRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.DateRangeFilterRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.ExportRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.FirebaseSettingsRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.JsonConverterRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.ReminderTimeRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.SettingsRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.ThemeRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.TransactionRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.VersionCheckerRepositoryImpl
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.BudgetRepository
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import com.naveenapps.expensemanager.core.repository.CountryRepository
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import com.naveenapps.expensemanager.core.repository.DateRangeFilterRepository
import com.naveenapps.expensemanager.core.repository.ExportRepository
import com.naveenapps.expensemanager.core.repository.FirebaseSettingsRepository
import com.naveenapps.expensemanager.core.repository.JsonConverterRepository
import com.naveenapps.expensemanager.core.repository.ReminderTimeRepository
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import com.naveenapps.expensemanager.core.repository.ThemeRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import com.naveenapps.expensemanager.core.repository.VersionCheckerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependence injection repository module. This will create repository and use cases related object
 * and it's relations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun provideCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl,
    ): CategoryRepository

    @Singleton
    @Binds
    abstract fun provideAccountRepository(
        accountRepositoryImpl: AccountRepositoryImpl,
    ): AccountRepository

    @Singleton
    @Binds
    abstract fun provideTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl,
    ): TransactionRepository

    @Singleton
    @Binds
    abstract fun provideThemeRepository(
        themeRepositoryImpl: ThemeRepositoryImpl,
    ): ThemeRepository

    @Singleton
    @Binds
    abstract fun provideCurrencyRepository(
        currencyRepositoryImpl: CurrencyRepositoryImpl,
    ): CurrencyRepository

    @Singleton
    @Binds
    abstract fun provideReminderTimeRepository(
        reminderTimeRepositoryImpl: ReminderTimeRepositoryImpl,
    ): ReminderTimeRepository

    @Singleton
    @Binds
    abstract fun provideSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl,
    ): SettingsRepository

    @Singleton
    @Binds
    abstract fun provideBudgetRepository(
        budgetRepositoryImpl: BudgetRepositoryImpl,
    ): BudgetRepository

    @Singleton
    @Binds
    abstract fun provideExportRepository(
        exportRepositoryImpl: ExportRepositoryImpl,
    ): ExportRepository

    @Singleton
    @Binds
    abstract fun provideDateRangeFilterRepository(
        dateRangeFilterRepositoryImpl: DateRangeFilterRepositoryImpl,
    ): DateRangeFilterRepository

    @Singleton
    @Binds
    abstract fun provideVersionCheckerRepository(
        versionCheckerRepositoryImpl: VersionCheckerRepositoryImpl,
    ): VersionCheckerRepository

    @Singleton
    @Binds
    abstract fun provideJsonConverterRepository(
        jsonConverterRepositoryImpl: JsonConverterRepositoryImpl,
    ): JsonConverterRepository

    @Singleton
    @Binds
    abstract fun provideCountryRepository(
        countryRepositoryImpl: CountryRepositoryImpl,
    ): CountryRepository

    @Singleton
    @Binds
    abstract fun provideFirebaseSettingsRepository(
        firebaseSettingsRepositoryImpl: FirebaseSettingsRepositoryImpl,
    ): FirebaseSettingsRepository
}
