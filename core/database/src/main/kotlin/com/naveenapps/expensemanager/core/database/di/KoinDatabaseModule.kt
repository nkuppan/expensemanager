import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import org.koin.android.ext.koin.androidContext

import org.koin.dsl.module

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE account ADD COLUMN sequence INTEGER NOT NULL DEFAULT ${Int.MAX_VALUE}")
    }
}
private const val DATA_BASE_NAME = "expense_manager_database.db"

val DatabaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            ExpenseManagerDatabase::class.java,
            DATA_BASE_NAME,
        ).addMigrations(
            MIGRATION_2_3,
        ).build()
    }
    single { get<ExpenseManagerDatabase>().categoryDao() }
    single { get<ExpenseManagerDatabase>().accountDao() }
    single { get<ExpenseManagerDatabase>().transactionDao() }
    single { get<ExpenseManagerDatabase>().budgetDao() }
}

