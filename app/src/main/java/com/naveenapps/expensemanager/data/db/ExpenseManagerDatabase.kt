package com.naveenapps.expensemanager.data.db


import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.naveenapps.expensemanager.data.db.dao.AccountDao
import com.naveenapps.expensemanager.data.db.dao.BudgetDao
import com.naveenapps.expensemanager.data.db.dao.CategoryDao
import com.naveenapps.expensemanager.data.db.dao.TransactionDao
import com.naveenapps.expensemanager.data.db.entity.AccountEntity
import com.naveenapps.expensemanager.data.db.entity.BudgetAccountEntity
import com.naveenapps.expensemanager.data.db.entity.BudgetCategoryEntity
import com.naveenapps.expensemanager.data.db.entity.BudgetEntity
import com.naveenapps.expensemanager.data.db.entity.CategoryEntity
import com.naveenapps.expensemanager.data.db.entity.TransactionEntity
import com.naveenapps.expensemanager.data.db.utils.AccountTypeConverter
import com.naveenapps.expensemanager.data.db.utils.CategoryTypeConverter
import com.naveenapps.expensemanager.data.db.utils.DateConverter
import com.naveenapps.expensemanager.data.db.utils.TransactionTypeConverter

/**
 * The Room database for this app
 */
@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        AccountEntity::class,
        BudgetEntity::class,
        BudgetCategoryEntity::class,
        BudgetAccountEntity::class,
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

    abstract fun budgetDao(): BudgetDao
}
