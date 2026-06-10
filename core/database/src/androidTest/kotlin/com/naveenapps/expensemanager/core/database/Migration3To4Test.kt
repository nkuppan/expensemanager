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

// v3 budget columns: id, name, icon_background_color, icon_name, selected_month, amount,
//                    all_accounts_selected, all_categories_selected, created_on, updated_on
// v4 budget columns: id, selected_month, amount, all_accounts_selected,
//                    all_categories_selected, created_on, updated_on  (name + two icon cols dropped)

@RunWith(AndroidJUnit4::class)
class Migration3To4Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpenseManagerDatabase::class.java,
    )

    // region schema shape

    @Test
    @Throws(IOException::class)
    fun afterMigration_budgetTableHasCorrectColumns() {
        helper.createDatabase(TEST_DB, 3).close()

        val db = helper.runMigrationsAndValidate(
            TEST_DB,
            4,
            true,
            MIGRATION_3_4,
        )

        val cursor = db.query("PRAGMA table_info(budget)")
        val columnNames = buildList {
            while (cursor.moveToNext()) {
                add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
        }
        cursor.close()
        db.close()

        assertThat(columnNames).containsExactly(
            "id",
            "selected_month",
            "amount",
            "all_accounts_selected",
            "all_categories_selected",
            "created_on",
            "updated_on",
        )
    }

    @Test
    @Throws(IOException::class)
    fun afterMigration_droppedColumnsNoLongerExist() {
        helper.createDatabase(TEST_DB, 3).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_3_4)

        val cursor = db.query("PRAGMA table_info(budget)")
        val columnNames = buildList {
            while (cursor.moveToNext()) {
                add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
        }
        cursor.close()
        db.close()

        assertThat(columnNames).containsNoneOf("name", "icon_background_color", "icon_name")
    }

    // endregion

    // region data preservation

    @Test
    @Throws(IOException::class)
    fun afterMigration_existingBudgetRowsArePreserved() {
        val db3 = helper.createDatabase(TEST_DB, 3)
        db3.execSQL(
            """
            INSERT INTO budget
                (id, name, icon_background_color, icon_name, selected_month, amount,
                 all_accounts_selected, all_categories_selected, created_on, updated_on)
            VALUES
                ('budget-1', 'Groceries', '#FF0000', 'ic_food', 'June 2025', 500.0, 1, 0, 1000, 2000),
                ('budget-2', 'Transport', '#00FF00', 'ic_car',  'June 2025', 200.0, 0, 1, 3000, 4000)
            """.trimIndent()
        )
        db3.close()

        val db4 = helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_3_4)
        val cursor = db4.query("SELECT * FROM budget ORDER BY id")

        assertThat(cursor.count).isEqualTo(2)

        cursor.moveToFirst()
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("id"))).isEqualTo("budget-1")
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("selected_month"))).isEqualTo("June 2025")
        assertThat(cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))).isEqualTo(500.0)
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow("all_accounts_selected"))).isEqualTo(1)
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow("all_categories_selected"))).isEqualTo(0)
        assertThat(cursor.getLong(cursor.getColumnIndexOrThrow("created_on"))).isEqualTo(1000L)
        assertThat(cursor.getLong(cursor.getColumnIndexOrThrow("updated_on"))).isEqualTo(2000L)

        cursor.moveToNext()
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("id"))).isEqualTo("budget-2")
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("selected_month"))).isEqualTo("June 2025")
        assertThat(cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))).isEqualTo(200.0)
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow("all_accounts_selected"))).isEqualTo(0)
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow("all_categories_selected"))).isEqualTo(1)
        assertThat(cursor.getLong(cursor.getColumnIndexOrThrow("created_on"))).isEqualTo(3000L)
        assertThat(cursor.getLong(cursor.getColumnIndexOrThrow("updated_on"))).isEqualTo(4000L)

        cursor.close()
        db4.close()
    }

    @Test
    @Throws(IOException::class)
    fun afterMigration_emptyBudgetTableRemainsEmpty() {
        helper.createDatabase(TEST_DB, 3).close()

        val db4 = helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_3_4)
        val cursor = db4.query("SELECT COUNT(*) FROM budget")
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db4.close()

        assertThat(count).isEqualTo(0)
    }

    // endregion

    // region other tables unaffected

    @Test
    @Throws(IOException::class)
    fun afterMigration_otherTablesAreUntouched() {
        val db3 = helper.createDatabase(TEST_DB, 3)
        db3.execSQL(
            """
            INSERT INTO account
                (id, name, type, icon_background_color, icon_name, amount, credit_limit,
                 sequence, created_on, updated_on)
            VALUES
                ('acc-1', 'Checking', 0, '#FFFFFF', 'ic_bank', 1000.0, 0.0, 1, 1000, 2000)
            """.trimIndent()
        )
        db3.close()

        val db4 = helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_3_4)
        val cursor = db4.query("SELECT id, name FROM account")
        cursor.moveToFirst()
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("id"))).isEqualTo("acc-1")
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("name"))).isEqualTo("Checking")
        cursor.close()
        db4.close()
    }

    // endregion
}
