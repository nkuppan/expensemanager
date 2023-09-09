package com.nkuppan.expensemanager.data.db


import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nkuppan.expensemanager.data.db.dao.AccountDao
import com.nkuppan.expensemanager.data.db.dao.CategoryDao
import com.nkuppan.expensemanager.data.db.dao.TransactionDao
import com.nkuppan.expensemanager.data.db.entity.AccountEntity
import com.nkuppan.expensemanager.data.db.entity.CategoryEntity
import com.nkuppan.expensemanager.data.db.entity.TransactionEntity
import com.nkuppan.expensemanager.data.db.utils.DateConverter

/**
 * The Room database for this app
 */
@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        AccountEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class ExpenseManagerDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao

    abstract fun transactionDao(): TransactionDao

    abstract fun accountDao(): AccountDao
}
