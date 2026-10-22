package com.naveenapps.expensemanager.core.database.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.naveenapps.expensemanager.core.database.ALL_MIGRATIONS
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.database.PendingDatabaseRestore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val DATA_BASE_NAME = "expense_manager_database.db"

private fun buildDatabase(context: Context): ExpenseManagerDatabase = Room.databaseBuilder(
    context,
    ExpenseManagerDatabase::class.java,
    DATA_BASE_NAME,
).addMigrations(*ALL_MIGRATIONS).build()

val DatabaseModule = module {
    single {
        val context = androidContext()
        // A restore is staged and then applied here, before Room opens the file.
        if (!PendingDatabaseRestore.applyIfPending(context, DATA_BASE_NAME)) {
            return@single buildDatabase(context)
        }
        // Open the restored file now, so a backup Room can't migrate or validate is caught
        // here and the previous database put back, instead of crashing on every launch.
        val restored = buildDatabase(context)
        try {
            restored.openHelper.writableDatabase
            PendingDatabaseRestore.confirmRestored(context)
            restored
        } catch (e: Exception) {
            Log.e("DatabaseModule", "Restored database couldn't be opened, rolling back", e)
            restored.close()
            PendingDatabaseRestore.rollback(context, DATA_BASE_NAME)
            buildDatabase(context)
        }
    }
    single { get<ExpenseManagerDatabase>().categoryDao() }
    single { get<ExpenseManagerDatabase>().accountDao() }
    single { get<ExpenseManagerDatabase>().transactionDao() }
    single { get<ExpenseManagerDatabase>().budgetDao() }
    single { get<ExpenseManagerDatabase>().recurringTransactionDao() }
}
