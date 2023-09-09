package com.nkuppan.expensemanager.data.di

import com.nkuppan.expensemanager.data.mappers.*
import com.nkuppan.expensemanager.data.repository.*
import com.nkuppan.expensemanager.domain.repository.AccountRepository
import com.nkuppan.expensemanager.domain.repository.CategoryRepository
import com.nkuppan.expensemanager.domain.repository.CurrencyRepository
import com.nkuppan.expensemanager.domain.repository.SettingsRepository
import com.nkuppan.expensemanager.domain.repository.ThemeRepository
import com.nkuppan.expensemanager.domain.repository.TransactionRepository
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
    abstract fun provideSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl,
    ): SettingsRepository
}