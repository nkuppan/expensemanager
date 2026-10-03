package com.naveenapps.expensemanager.core.database.di

import androidx.room.Room
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.database.MIGRATION_2_3
import com.naveenapps.expensemanager.core.database.MIGRATION_3_4
import com.naveenapps.expensemanager.core.database.MIGRATION_4_5
import com.naveenapps.expensemanager.core.database.MIGRATION_5_6
import com.naveenapps.expensemanager.core.database.MIGRATION_6_7
import com.naveenapps.expensemanager.core.database.MIGRATION_7_8
import com.naveenapps.expensemanager.core.database.PendingDatabaseRestore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val DATA_BASE_NAME = "expense_manager_database.db"

val DatabaseModule = module {
    single {
        // A restore is staged and then applied here, before Room opens the file.
        PendingDatabaseRestore.applyIfPending(androidContext(), DATA_BASE_NAME)
        Room.databaseBuilder(
            androidContext(),
            ExpenseManagerDatabase::class.java,
            DATA_BASE_NAME,
        ).addMigrations(
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8,
        ).build()
    }
    single { get<ExpenseManagerDatabase>().categoryDao() }
    single { get<ExpenseManagerDatabase>().accountDao() }
    single { get<ExpenseManagerDatabase>().transactionDao() }
    single { get<ExpenseManagerDatabase>().budgetDao() }
}
