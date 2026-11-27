package com.naveenapps.expensemanager.core.database.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.useWriterConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.naveenapps.expensemanager.core.database.ALL_MIGRATIONS
import com.naveenapps.expensemanager.core.database.DATABASE_NAME
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.database.PendingDatabaseRestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// Room on the SQLiteDriver APIs with the framework SQLite (AndroidSQLiteDriver): the same engine
// and file as before, without SupportSQLite (OpenSpec change move-database-to-sqlite-driver).
private fun buildDatabase(context: Context): ExpenseManagerDatabase = Room.databaseBuilder(
    context,
    ExpenseManagerDatabase::class.java,
    DATABASE_NAME,
).setDriver(AndroidSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .addMigrations(*ALL_MIGRATIONS)
    .build()

/**
 * Opens [database] now: Room opens the file, runs any migrations and validates the schema on the
 * first connection, so a backup it can't open fails here, as opening the SupportSQLite helper
 * did. Koin builds the database synchronously, so this blocks like that call did.
 */
private fun openNow(database: ExpenseManagerDatabase) = runBlocking {
    database.useWriterConnection { transactor -> transactor.usePrepared("PRAGMA user_version") { it.step() } }
}

val DatabaseModule = module {
    single {
        val context = androidContext()
        // A restore is staged and then applied here, before Room opens the file.
        if (!PendingDatabaseRestore.applyIfPending(context, DATABASE_NAME)) {
            return@single buildDatabase(context)
        }
        // Open the restored file now, so a backup Room can't migrate or validate is caught
        // here and the previous database put back, instead of crashing on every launch.
        val restored = buildDatabase(context)
        try {
            openNow(restored)
            PendingDatabaseRestore.confirmRestored(context)
            restored
        } catch (e: Exception) {
            Log.e("DatabaseModule", "Restored database couldn't be opened, rolling back", e)
            restored.close()
            PendingDatabaseRestore.rollback(context, DATABASE_NAME)
            buildDatabase(context)
        }
    }
    single { get<ExpenseManagerDatabase>().categoryDao() }
    single { get<ExpenseManagerDatabase>().accountDao() }
    single { get<ExpenseManagerDatabase>().transactionDao() }
    single { get<ExpenseManagerDatabase>().budgetDao() }
    single { get<ExpenseManagerDatabase>().recurringTransactionDao() }
    single { get<ExpenseManagerDatabase>().debtDao() }
}
