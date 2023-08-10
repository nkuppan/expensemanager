package com.nkuppan.expensemanager.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nkuppan.expensemanager.core.model.Account
import com.nkuppan.expensemanager.core.model.Category
import com.nkuppan.expensemanager.core.model.CategoryType
import com.nkuppan.expensemanager.core.model.PaymentMode
import com.nkuppan.expensemanager.data.db.ExpenseManagerDatabase
import com.nkuppan.expensemanager.data.db.dao.AccountDao
import com.nkuppan.expensemanager.data.db.dao.CategoryDao
import com.nkuppan.expensemanager.data.db.dao.TransactionDao
import com.nkuppan.expensemanager.data.mappers.toEntityModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    private const val DATA_BASE_NAME = "expense_manager_database.db"

    private fun setupBaseValues(database: ExpenseManagerDatabase) {

        CoroutineScope(Dispatchers.IO).launch {

            val categoryCount = database.categoryDao().getCount()

            if (categoryCount == 0L) {
                BASE_CATEGORY_LIST.forEach { value ->
                    val categoryEntity = value.toEntityModel()
                    database.categoryDao().insert(categoryEntity)
                }
            }

            val accountCount = database.accountDao().getCount()

            if (accountCount == 0L) {
                BASE_ACCOUNT_LIST.forEach { value ->
                    val accountEntity = value.toEntityModel()
                    database.accountDao().insert(accountEntity)
                }
            }
        }
    }


    @Singleton
    @Provides
    fun provideRoomDatabase(
        @ApplicationContext context: Context
    ): ExpenseManagerDatabase {

        var database: ExpenseManagerDatabase? = null

        database = Room.databaseBuilder(
            context,
            ExpenseManagerDatabase::class.java,
            DATA_BASE_NAME
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                if (database != null && database is ExpenseManagerDatabase) {
                    setupBaseValues(database as ExpenseManagerDatabase)
                }
            }
        }).build()

        return database
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
}


val BASE_CATEGORY_LIST = listOf(
    Category(
        id = "1",
        name = "Clothing",
        type = CategoryType.EXPENSE,
        backgroundColor = "#F44336",
        isFavorite = false,
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "2",
        name = "Entertainment",
        type = CategoryType.EXPENSE,
        backgroundColor = "#E91E63",
        isFavorite = false,
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "3",
        name = "Food",
        type = CategoryType.EXPENSE,
        backgroundColor = "#9C27B0",
        isFavorite = false,
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "4",
        name = "Health",
        type = CategoryType.EXPENSE,
        backgroundColor = "#673AB7",
        isFavorite = false,
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "5",
        name = "Leisure",
        type = CategoryType.EXPENSE,
        backgroundColor = "#3F51B5",
        isFavorite = false,
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "6",
        name = "Shopping",
        type = CategoryType.EXPENSE,
        backgroundColor = "#2196F3",
        isFavorite = false,
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "7",
        name = "Transportation",
        type = CategoryType.EXPENSE,
        backgroundColor = "#03A9F4",
        isFavorite = false,
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "8",
        name = "Utilities",
        type = CategoryType.EXPENSE,
        backgroundColor = "#00BCD4",
        isFavorite = false,
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "9",
        name = "Salary",
        type = CategoryType.INCOME,
        backgroundColor = "#4CAF50",
        isFavorite = false,
        createdOn = Date(),
        updatedOn = Date(),
    )
)

val BASE_ACCOUNT_LIST = listOf(
    Account(
        "1",
        "Wallet",
        PaymentMode.WALLET,
        "#4CAF50",
        Calendar.getInstance().time,
        Calendar.getInstance().time,
    ),
    Account(
        "2",
        "Card-xxx",
        PaymentMode.CARD,
        "#4CAF50",
        Calendar.getInstance().time,
        Calendar.getInstance().time,
    )
)