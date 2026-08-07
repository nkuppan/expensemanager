package com.naveenapps.expensemanager.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

private const val TEST_DB = "migration_test"

@RunWith(AndroidJUnit4::class)
class Migration5To6Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpenseManagerDatabase::class.java,
    )

    // region schema shape

    @Test
    @Throws(IOException::class)
    fun afterMigration_budgetTableHasNewColumn() {
        helper.createDatabase(TEST_DB, 5).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6)

        val cursor = db.query("PRAGMA table_info(budget)")
        val columnNames = buildList {
            while (cursor.moveToNext()) {
                add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
        }
        cursor.close()
        db.close()

        assertThat(columnNames).contains("period_type")
    }

    // endregion

    // region backfill for existing installs

    @Test
    @Throws(IOException::class)
    fun afterMigration_existingBudgetsAreBackfilledAsMonthly() {
        val db5 = helper.createDatabase(TEST_DB, 5)
        db5.execSQL(
            """
            INSERT INTO budget
                (id, selected_month, amount, all_accounts_selected, all_categories_selected,
                 created_on, updated_on)
            VALUES
                ('budget-1', 'August 2026', 500.0, 1, 1, 1000, 1000)
            """.trimIndent()
        )
        db5.close()

        val db6 = helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6)
        val cursor = db6.query("SELECT period_type FROM budget WHERE id = 'budget-1'")

        cursor.moveToFirst()
        // BudgetPeriod.MONTHLY.ordinal == 0
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow("period_type"))).isEqualTo(0)

        cursor.close()
        db6.close()
    }

    // endregion

    // region other tables unaffected

    @Test
    @Throws(IOException::class)
    fun afterMigration_otherTablesAreUntouched() {
        val db5 = helper.createDatabase(TEST_DB, 5)
        db5.execSQL(
            """
            INSERT INTO account
                (id, name, type, icon_background_color, icon_name, amount, credit_limit,
                 sequence, created_on, updated_on)
            VALUES
                ('acc-1', 'Checking', 0, '#FFFFFF', 'ic_bank', 1000.0, 0.0, 1, 1000, 2000)
            """.trimIndent()
        )
        db5.close()

        val db6 = helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6)
        val cursor = db6.query("SELECT id, name FROM account")
        cursor.moveToFirst()
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("id"))).isEqualTo("acc-1")
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("name"))).isEqualTo("Checking")
        cursor.close()
        db6.close()
    }

    // endregion
}
