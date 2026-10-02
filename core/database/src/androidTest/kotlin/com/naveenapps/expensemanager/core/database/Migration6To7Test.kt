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
class Migration6To7Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpenseManagerDatabase::class.java,
    )

    // region schema shape

    @Test
    @Throws(IOException::class)
    fun afterMigration_categoryTableHasNewColumn() {
        helper.createDatabase(TEST_DB, 6).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7)

        val cursor = db.query("PRAGMA table_info(category)")
        val columnNames = buildList {
            while (cursor.moveToNext()) {
                add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
        }
        cursor.close()
        db.close()

        assertThat(columnNames).contains("custom_image_path")
    }

    @Test
    @Throws(IOException::class)
    fun afterMigration_accountTableHasNewColumn() {
        helper.createDatabase(TEST_DB, 6).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7)

        val cursor = db.query("PRAGMA table_info(account)")
        val columnNames = buildList {
            while (cursor.moveToNext()) {
                add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
        }
        cursor.close()
        db.close()

        assertThat(columnNames).contains("custom_image_path")
    }

    // endregion

    // region backfill for existing installs

    @Test
    @Throws(IOException::class)
    fun afterMigration_existingCategoriesHaveNullImagePath() {
        val db6 = helper.createDatabase(TEST_DB, 6)
        db6.execSQL(
            """
            INSERT INTO category
                (id, name, type, icon_background_color, icon_name, updated_on, created_on)
            VALUES
                ('cat-1', 'Food', 0, '#43A546', 'ic_calendar', 1000, 1000)
            """.trimIndent(),
        )
        db6.close()

        val db7 = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7)
        val cursor = db7.query("SELECT custom_image_path FROM category WHERE id = 'cat-1'")

        cursor.moveToFirst()
        assertThat(cursor.isNull(cursor.getColumnIndexOrThrow("custom_image_path"))).isTrue()

        cursor.close()
        db7.close()
    }

    @Test
    @Throws(IOException::class)
    fun afterMigration_existingAccountsHaveNullImagePath() {
        val db6 = helper.createDatabase(TEST_DB, 6)
        db6.execSQL(
            """
            INSERT INTO account
                (id, name, type, icon_background_color, icon_name, amount, credit_limit,
                 sequence, created_on, updated_on)
            VALUES
                ('acc-1', 'Checking', 0, '#FFFFFF', 'ic_bank', 1000.0, 0.0, 1, 1000, 2000)
            """.trimIndent(),
        )
        db6.close()

        val db7 = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7)
        val cursor = db7.query("SELECT custom_image_path FROM account WHERE id = 'acc-1'")

        cursor.moveToFirst()
        assertThat(cursor.isNull(cursor.getColumnIndexOrThrow("custom_image_path"))).isTrue()

        cursor.close()
        db7.close()
    }

    // endregion

    // region other tables unaffected

    @Test
    @Throws(IOException::class)
    fun afterMigration_otherTablesAreUntouched() {
        val db6 = helper.createDatabase(TEST_DB, 6)
        db6.execSQL(
            """
            INSERT INTO budget
                (id, selected_month, amount, all_accounts_selected, all_categories_selected,
                 created_on, updated_on, period_type)
            VALUES
                ('budget-1', '2024-01', 500.0, 1, 1, 1000, 2000, 0)
            """.trimIndent(),
        )
        db6.close()

        val db7 = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7)
        val cursor = db7.query("SELECT id, amount FROM budget")
        cursor.moveToFirst()
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("id"))).isEqualTo("budget-1")
        assertThat(cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))).isEqualTo(500.0)
        cursor.close()
        db7.close()
    }

    // endregion
}
