package com.naveenapps.expensemanager.core.database.di

import android.content.Context
import androidx.room.Room
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.database.dao.AccountDao
import com.naveenapps.expensemanager.core.database.dao.BudgetDao
import com.naveenapps.expensemanager.core.database.dao.CategoryDao
import com.naveenapps.expensemanager.core.database.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.*
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    private const val DATA_BASE_NAME = "expense_manager_database.db"


    @Singleton
    @Provides
    fun provideRoomDatabase(@ApplicationContext context: Context): ExpenseManagerDatabase {
        return Room.databaseBuilder(
            context,
            ExpenseManagerDatabase::class.java,
            DATA_BASE_NAME
        ).build()
    }

    @Singleton
    @Provides
    fun provideCategoryDao(database: ExpenseManagerDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Singleton
    @Provides
    fun provideAccountDao(database: ExpenseManagerDatabase): AccountDao {
        return database.accountDao()
    }

    @Singleton
    @Provides
    fun provideTransactionDao(database: ExpenseManagerDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Singleton
    @Provides
    fun provideBudgetDao(database: ExpenseManagerDatabase): BudgetDao {
        return database.budgetDao()
    }
}