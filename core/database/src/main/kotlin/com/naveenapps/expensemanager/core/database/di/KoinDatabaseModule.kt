package com.naveenapps.expensemanager.core.database.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.database.LegacyCurrencyCodeProvider
import com.naveenapps.expensemanager.core.database.PendingDatabaseRestore
import com.naveenapps.expensemanager.core.database.allMigrations
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val DATA_BASE_NAME = "expense_manager_database.db"

private fun buildDatabase(
    context: Context,
    legacyCurrencyCodeProvider: LegacyCurrencyCodeProvider,
): ExpenseManagerDatabase = Room.databaseBuilder(
    context,
    ExpenseManagerDatabase::class.java,
    DATA_BASE_NAME,
).addMigrations(*allMigrations(legacyCurrencyCodeProvider)).build()

val DatabaseModule = module {
    single {
        val context = androidContext()
        val legacyCurrencyCodeProvider = get<LegacyCurrencyCodeProvider>()
        // A restore is staged and then applied here, before Room opens the file.
        if (!PendingDatabaseRestore.applyIfPending(context, DATA_BASE_NAME)) {
            return@single buildDatabase(context, legacyCurrencyCodeProvider)
        }
        // Open the restored file now, so a backup Room can't migrate or validate is caught
        // here and the previous database put back, instead of crashing on every launch.
        val restored = buildDatabase(context, legacyCurrencyCodeProvider)
        try {
            restored.openHelper.writableDatabase
            PendingDatabaseRestore.confirmRestored(context)
            restored
        } catch (e: Exception) {
            Log.e("DatabaseModule", "Restored database couldn't be opened, rolling back", e)
            restored.close()
            PendingDatabaseRestore.rollback(context, DATA_BASE_NAME)
            buildDatabase(context, legacyCurrencyCodeProvider)
        }
    }
    single { get<ExpenseManagerDatabase>().categoryDao() }
    single { get<ExpenseManagerDatabase>().accountDao() }
    single { get<ExpenseManagerDatabase>().transactionDao() }
    single { get<ExpenseManagerDatabase>().budgetDao() }
    single { get<ExpenseManagerDatabase>().recurringTransactionDao() }
    single { get<ExpenseManagerDatabase>().exchangeRateDao() }
    single { get<ExpenseManagerDatabase>().exchangeRateSnapshotDao() }
    single { get<ExpenseManagerDatabase>().staticExchangeRateDao() }
}
