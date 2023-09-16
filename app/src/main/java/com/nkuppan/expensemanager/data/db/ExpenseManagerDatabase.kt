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
import com.nkuppan.expensemanager.data.db.utils.AccountTypeConverter
import com.nkuppan.expensemanager.data.db.utils.CategoryTypeConverter
import com.nkuppan.expensemanager.data.db.utils.DateConverter
import com.nkuppan.expensemanager.data.db.utils.TransactionTypeConverter

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
@TypeConverters(
    DateConverter::class,
    TransactionTypeConverter::class,
    CategoryTypeConverter::class,
    AccountTypeConverter::class,
)
abstract class ExpenseManagerDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao

    abstract fun transactionDao(): TransactionDao

    abstract fun accountDao(): AccountDao
}
