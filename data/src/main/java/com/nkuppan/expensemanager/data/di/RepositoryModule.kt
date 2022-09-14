package com.nkuppan.expensemanager.data.di

import com.nkuppan.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.nkuppan.expensemanager.data.datastore.CurrencyDataStore
import com.nkuppan.expensemanager.data.datastore.SettingsDataStore
import com.nkuppan.expensemanager.data.datastore.ThemeDataStore
import com.nkuppan.expensemanager.data.db.dao.AccountDao
import com.nkuppan.expensemanager.data.db.dao.CategoryDao
import com.nkuppan.expensemanager.data.db.dao.TransactionDao
import com.nkuppan.expensemanager.data.mappers.*
import com.nkuppan.expensemanager.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependence injection repository module. This will create repository and use cases related object
 * and it's relations.
 */
@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryDao: CategoryDao,
        appCoroutineDispatchers: AppCoroutineDispatchers
    ): CategoryRepository {
        return CategoryRepositoryImpl(
            categoryDao,
            CategoryDomainEntityMapper(),
            CategoryEntityDomainMapper(),
            appCoroutineDispatchers
        )
    }

    @Provides
    @Singleton
    fun provideAccountRepository(
        accountDao: AccountDao,
        appCoroutineDispatchers: AppCoroutineDispatchers
    ): AccountRepository {
        return AccountRepositoryImpl(
            accountDao,
            AccountDomainEntityMapper(),
            AccountEntityDomainMapper(),
            appCoroutineDispatchers
        )
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(
        transactionDao: TransactionDao,
        appCoroutineDispatchers: AppCoroutineDispatchers
    ): TransactionRepository {
        return TransactionRepositoryImpl(
            transactionDao,
            TransactionDomainEntityMapper(),
            TransactionEntityDomainMapper(),
            CategoryEntityDomainMapper(),
            AccountEntityDomainMapper(),
            appCoroutineDispatchers
        )
    }

    @Provides
    @Singleton
    fun provideThemeRepository(
        dataStore: ThemeDataStore,
        dispatchers: AppCoroutineDispatchers
    ): ThemeRepository {
        return ThemeRepositoryImpl(dataStore, dispatchers)
    }

    @Provides
    @Singleton
    fun provideCurrencyRepository(
        dataStore: CurrencyDataStore,
        dispatchers: AppCoroutineDispatchers
    ): CurrencyRepository {
        return CurrencyRepositoryImpl(dataStore, dispatchers)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        dataStore: SettingsDataStore,
        dispatchers: AppCoroutineDispatchers
    ): SettingsRepository {
        return SettingsRepositoryImpl(dataStore, dispatchers)
    }
}
