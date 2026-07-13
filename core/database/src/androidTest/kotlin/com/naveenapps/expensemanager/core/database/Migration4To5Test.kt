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
class Migration4To5Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpenseManagerDatabase::class.java,
    )

    // region schema shape

    @Test
    @Throws(IOException::class)
    fun afterMigration_categoryTableHasNewColumn() {
        helper.createDatabase(TEST_DB, 4).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_4_5)

        val cursor = db.query("PRAGMA table_info(category)")
        val columnNames = buildList {
            while (cursor.moveToNext()) {
                add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
        }
        cursor.close()
        db.close()

        assertThat(columnNames).contains("default_category_key")
    }

    // endregion

    // region backfill for existing installs

    @Test
    @Throws(IOException::class)
    fun afterMigration_seededDefaultCategoriesAreBackfilled() {
        val db4 = helper.createDatabase(TEST_DB, 4)
        db4.execSQL(
            """
            INSERT INTO category
                (id, name, type, icon_background_color, icon_name, updated_on, created_on)
            VALUES
                ('1', 'Clothing', 0, '#F44336', 'apparel', 1000, 1000),
                ('9', 'Salary', 1, '#4CAF50', 'savings', 1000, 1000)
            """.trimIndent()
        )
        db4.close()

        val db5 = helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_4_5)
        val cursor = db5.query("SELECT id, default_category_key FROM category ORDER BY id")

        cursor.moveToFirst()
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("id"))).isEqualTo("1")
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("default_category_key")))
            .isEqualTo("clothing")

        cursor.moveToNext()
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("id"))).isEqualTo("9")
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("default_category_key")))
            .isEqualTo("salary")

        cursor.close()
        db5.close()
    }

    @Test
    @Throws(IOException::class)
    fun afterMigration_renamedDefaultCategoryIsNotBackfilled() {
        val db4 = helper.createDatabase(TEST_DB, 4)
        db4.execSQL(
            """
            INSERT INTO category
                (id, name, type, icon_background_color, icon_name, updated_on, created_on)
            VALUES
                ('3', 'Groceries', 0, '#9C27B0', 'restaurant', 1000, 1000)
            """.trimIndent()
        )
        db4.close()

        // id "3" is normally the seeded "Food" category, but this row was renamed by the user
        // before the migration ran, so it must NOT be marked as a default category.
        val db5 = helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_4_5)
        val cursor = db5.query("SELECT default_category_key FROM category WHERE id = '3'")

        cursor.moveToFirst()
        assertThat(cursor.isNull(cursor.getColumnIndexOrThrow("default_category_key"))).isTrue()

        cursor.close()
        db5.close()
    }

    @Test
    @Throws(IOException::class)
    fun afterMigration_userCreatedCategoryIsNotBackfilled() {
        val db4 = helper.createDatabase(TEST_DB, 4)
        db4.execSQL(
            """
            INSERT INTO category
                (id, name, type, icon_background_color, icon_name, updated_on, created_on)
            VALUES
                ('custom-uuid-123', 'My Hobby', 0, '#123456', 'ic_custom', 1000, 1000)
            """.trimIndent()
        )
        db4.close()

        val db5 = helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_4_5)
        val cursor = db5.query(
            "SELECT default_category_key FROM category WHERE id = 'custom-uuid-123'"
        )

        cursor.moveToFirst()
        assertThat(cursor.isNull(cursor.getColumnIndexOrThrow("default_category_key"))).isTrue()

        cursor.close()
        db5.close()
    }

    // endregion

    // region other tables unaffected

    @Test
    @Throws(IOException::class)
    fun afterMigration_otherTablesAreUntouched() {
        val db4 = helper.createDatabase(TEST_DB, 4)
        db4.execSQL(
            """
            INSERT INTO account
                (id, name, type, icon_background_color, icon_name, amount, credit_limit,
                 sequence, created_on, updated_on)
            VALUES
                ('acc-1', 'Checking', 0, '#FFFFFF', 'ic_bank', 1000.0, 0.0, 1, 1000, 2000)
            """.trimIndent()
        )
        db4.close()

        val db5 = helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_4_5)
        val cursor = db5.query("SELECT id, name FROM account")
        cursor.moveToFirst()
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("id"))).isEqualTo("acc-1")
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("name"))).isEqualTo("Checking")
        cursor.close()
        db5.close()
    }

    // endregion
}
