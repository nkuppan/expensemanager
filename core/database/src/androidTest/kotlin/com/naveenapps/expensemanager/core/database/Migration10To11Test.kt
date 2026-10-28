package com.naveenapps.expensemanager.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import java.io.IOException
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB = "migration_test"

@RunWith(AndroidJUnit4::class)
class Migration10To11Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpenseManagerDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun existingBudgetsKeepTheirDataAndHaveNoName() {
        val db10 = helper.createDatabase(TEST_DB, 10)
        db10.execSQL(
            """
            INSERT INTO budget
                (id, selected_month, amount, period_type, all_accounts_selected,
                 all_categories_selected, created_on, updated_on)
            VALUES
                ('budget-1', 'October 2026', 500.0, 0, 1, 1, 1000, 1000)
            """.trimIndent(),
        )
        db10.close()

        val db11 = helper.runMigrationsAndValidate(TEST_DB, 11, true, MIGRATION_10_11)
        db11.query("SELECT name, selected_month, amount FROM budget WHERE id = 'budget-1'").use { cursor ->
            cursor.moveToFirst()
            assertThat(cursor.isNull(0)).isTrue()
            assertThat(cursor.getString(1)).isEqualTo("October 2026")
            assertThat(cursor.getDouble(2)).isEqualTo(500.0)
        }
        db11.close()
    }

    @Test
    @Throws(IOException::class)
    fun aNameCanBeStoredAfterMigrating() {
        helper.createDatabase(TEST_DB, 10).close()

        val db11 = helper.runMigrationsAndValidate(TEST_DB, 11, true, MIGRATION_10_11)
        db11.execSQL(
            """
            INSERT INTO budget
                (id, name, selected_month, amount, period_type, all_accounts_selected,
                 all_categories_selected, created_on, updated_on)
            VALUES
                ('budget-2', 'Groceries', 'October 2026', 300.0, 0, 1, 1, 1000, 1000)
            """.trimIndent(),
        )
        db11.query("SELECT name FROM budget WHERE id = 'budget-2'").use { cursor ->
            cursor.moveToFirst()
            assertThat(cursor.getString(0)).isEqualTo("Groceries")
        }
        db11.close()
    }
}
