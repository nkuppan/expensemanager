package com.naveenapps.expensemanager.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import java.io.IOException
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB = "migration_test"

@RunWith(AndroidJUnit4::class)
class Migration7To8Test {

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = InstrumentationRegistry.getInstrumentation(),
        file = InstrumentationRegistry.getInstrumentation().targetContext.getDatabasePath(TEST_DB),
        driver = AndroidSQLiteDriver(),
        databaseClass = ExpenseManagerDatabase::class,
    )

    // The driver-based helper reuses one file and opens whatever is there; the SupportSQLite one
    // deleted it first. Start every test from no database, as before.
    @Before
    fun deleteTestDatabase() {
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(TEST_DB)
    }

    // region schema shape

    @Test
    @Throws(IOException::class)
    fun afterMigration_transactionAttachmentTableExists() {
        helper.createDatabase(7).close()

        val db = helper.runMigrationsAndValidate(8, listOf(MIGRATION_7_8))

        val cursor = db.query("PRAGMA table_info(transaction_attachment)")
        val columnNames = buildList {
            while (cursor.moveToNext()) {
                add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
        }
        cursor.close()
        db.close()

        assertThat(columnNames).containsAtLeast("id", "transaction_id", "image_path", "created_on")
    }

    // endregion

    // region cascade delete

    @Test
    @Throws(IOException::class)
    fun afterMigration_deletingTransactionCascadesToAttachments() {
        val db7 = helper.createDatabase(7)
        db7.execSQL(
            """
            INSERT INTO category
                (id, name, type, icon_background_color, icon_name, updated_on, created_on)
            VALUES
                ('cat-1', 'Food', 0, '#43A546', 'ic_calendar', 1000, 1000)
            """.trimIndent(),
        )
        db7.execSQL(
            """
            INSERT INTO account
                (id, name, type, icon_background_color, icon_name, amount, credit_limit,
                 sequence, created_on, updated_on)
            VALUES
                ('acc-1', 'Checking', 0, '#FFFFFF', 'ic_bank', 1000.0, 0.0, 1, 1000, 2000)
            """.trimIndent(),
        )
        db7.execSQL(
            """
            INSERT INTO `transaction`
                (id, notes, category_id, from_account_id, type, amount, image_path,
                 created_on, updated_on, to_account_id)
            VALUES
                ('txn-1', '', 'cat-1', 'acc-1', 0, 100.0, '', 1000, 1000, NULL)
            """.trimIndent(),
        )
        db7.close()

        val db8 = helper.runMigrationsAndValidate(8, listOf(MIGRATION_7_8))
        // The app's Room connections enforce foreign keys; the helper's raw connection doesn't
        // unless asked (the SupportSQLite helper's did), and the cascade needs them.
        db8.execSQL("PRAGMA foreign_keys = ON")
        db8.execSQL(
            """
            INSERT INTO transaction_attachment (id, transaction_id, image_path, created_on)
            VALUES ('att-1', 'txn-1', '/tmp/receipt.jpg', 1000)
            """.trimIndent(),
        )

        val beforeDelete = db8.query("SELECT * FROM transaction_attachment WHERE transaction_id = 'txn-1'")
        assertThat(beforeDelete.count).isEqualTo(1)
        beforeDelete.close()

        db8.execSQL("DELETE FROM `transaction` WHERE id = 'txn-1'")

        val afterDelete = db8.query("SELECT * FROM transaction_attachment WHERE transaction_id = 'txn-1'")
        assertThat(afterDelete.count).isEqualTo(0)
        afterDelete.close()

        db8.close()
    }

    // endregion

    // region other tables unaffected

    @Test
    @Throws(IOException::class)
    fun afterMigration_otherTablesAreUntouched() {
        val db7 = helper.createDatabase(7)
        db7.execSQL(
            """
            INSERT INTO category
                (id, name, type, icon_background_color, icon_name, updated_on, created_on)
            VALUES
                ('cat-1', 'Food', 0, '#43A546', 'ic_calendar', 1000, 1000)
            """.trimIndent(),
        )
        db7.close()

        val db8 = helper.runMigrationsAndValidate(8, listOf(MIGRATION_7_8))
        val cursor = db8.query("SELECT id, name FROM category")
        cursor.moveToFirst()
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("id"))).isEqualTo("cat-1")
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("name"))).isEqualTo("Food")
        cursor.close()
        db8.close()
    }

    // endregion
}
