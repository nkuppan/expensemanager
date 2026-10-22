package com.naveenapps.expensemanager.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import java.io.IOException
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB = "migration_test"

/**
 * MIGRATION_9_10 only adds indices on `transaction` and `transaction_attachment`. These tests
 * check that the indices exist and match 10.json, that SQLite actually uses them, and that every
 * existing row (and the cascade deletes the app relies on) survive unchanged.
 */
@RunWith(AndroidJUnit4::class)
class Migration9To10Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpenseManagerDatabase::class.java,
    )

    // region schema

    @Test
    @Throws(IOException::class)
    fun afterMigration_transactionIndicesExistAndMatchSchema() {
        helper.createDatabase(TEST_DB, 9).close()

        // validateDroppedTables = true compares every table, indices included, with 10.json.
        val db = helper.runMigrationsAndValidate(TEST_DB, 10, true, MIGRATION_9_10)

        assertThat(indexNames(db, "transaction")).containsExactly(
            "index_transaction_created_on",
            "index_transaction_from_account_id",
            "index_transaction_to_account_id",
            "index_transaction_category_id",
        )
        assertThat(indexNames(db, "transaction_attachment"))
            .contains("index_transaction_attachment_transaction_id")
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun afterMigration_transactionQueriesUseTheNewIndices() {
        helper.createDatabase(TEST_DB, 9).close()
        val db = helper.runMigrationsAndValidate(TEST_DB, 10, true, MIGRATION_9_10)

        assertThat(queryPlan(db, "SELECT * FROM `transaction` WHERE from_account_id = 'acc-1'"))
            .contains("index_transaction_from_account_id")
        assertThat(queryPlan(db, "SELECT * FROM `transaction` WHERE category_id = 'cat-1'"))
            .contains("index_transaction_category_id")
        assertThat(
            queryPlan(db, "SELECT * FROM `transaction` WHERE created_on BETWEEN 0 AND 5000"),
        ).contains("index_transaction_created_on")
        assertThat(
            queryPlan(db, "SELECT * FROM transaction_attachment WHERE transaction_id = 'txn-1'"),
        ).contains("index_transaction_attachment_transaction_id")
        db.close()
    }

    // endregion

    // region data

    @Test
    @Throws(IOException::class)
    fun afterMigration_existingRowsAreUnchanged() {
        val db9 = helper.createDatabase(TEST_DB, 9)
        insertSampleData(db9)
        db9.close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 10, true, MIGRATION_9_10)

        db.query(
            "SELECT id, notes, category_id, from_account_id, to_account_id, type, amount, " +
                "created_on FROM `transaction` ORDER BY id",
        ).use { cursor ->
            assertThat(cursor.count).isEqualTo(2)

            cursor.moveToFirst()
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("id"))).isEqualTo("txn-1")
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("notes"))).isEqualTo("Lunch")
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("category_id"))).isEqualTo("cat-1")
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("from_account_id"))).isEqualTo("acc-1")
            assertThat(cursor.isNull(cursor.getColumnIndexOrThrow("to_account_id"))).isTrue()
            assertThat(cursor.getInt(cursor.getColumnIndexOrThrow("type"))).isEqualTo(0)
            assertThat(cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))).isEqualTo(100.5)
            assertThat(cursor.getLong(cursor.getColumnIndexOrThrow("created_on"))).isEqualTo(1000L)

            cursor.moveToNext()
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("id"))).isEqualTo("txn-2")
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("to_account_id"))).isEqualTo("acc-2")
            assertThat(cursor.getInt(cursor.getColumnIndexOrThrow("type"))).isEqualTo(2)
        }

        db.query("SELECT id, name, amount FROM account ORDER BY id").use { cursor ->
            assertThat(cursor.count).isEqualTo(2)
            cursor.moveToFirst()
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("name"))).isEqualTo("Checking")
            assertThat(cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))).isEqualTo(1000.0)
        }

        db.query("SELECT name FROM category").use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
            cursor.moveToFirst()
            assertThat(cursor.getString(0)).isEqualTo("Food")
        }

        db.query("SELECT transaction_id, image_path FROM transaction_attachment").use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
            cursor.moveToFirst()
            assertThat(cursor.getString(0)).isEqualTo("txn-1")
            assertThat(cursor.getString(1)).isEqualTo("/files/transaction_attachments/a.jpg")
        }

        assertThat(count(db, "recurring_transaction")).isEqualTo(1)
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun afterMigration_deletingAnAccountStillCascadesToItsTransactionsAndAttachments() {
        val db9 = helper.createDatabase(TEST_DB, 9)
        insertSampleData(db9)
        db9.close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 10, true, MIGRATION_9_10)
        // Room turns foreign keys on for app connections; do the same for this raw connection.
        db.execSQL("PRAGMA foreign_keys = ON")

        db.execSQL("DELETE FROM account WHERE id = 'acc-1'")

        assertThat(count(db, "transaction")).isEqualTo(0)
        assertThat(count(db, "transaction_attachment")).isEqualTo(0)
        assertThat(count(db, "recurring_transaction")).isEqualTo(0)
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun afterMigration_deletingACategoryStillCascadesToItsTransactions() {
        val db9 = helper.createDatabase(TEST_DB, 9)
        insertSampleData(db9)
        db9.close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 10, true, MIGRATION_9_10)
        db.execSQL("PRAGMA foreign_keys = ON")

        db.execSQL("DELETE FROM category WHERE id = 'cat-1'")

        assertThat(count(db, "transaction")).isEqualTo(0)
        assertThat(count(db, "account")).isEqualTo(2)
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun afterMigration_newTransactionsCanBeInsertedAndFoundByIndexedColumns() {
        val db9 = helper.createDatabase(TEST_DB, 9)
        insertSampleData(db9)
        db9.close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 10, true, MIGRATION_9_10)
        db.execSQL(
            """
            INSERT INTO `transaction`
                (id, notes, category_id, from_account_id, type, amount, image_path,
                 created_on, updated_on, to_account_id)
            VALUES ('txn-3', 'Coffee', 'cat-1', 'acc-2', 0, 3.5, '', 3000, 3000, NULL)
            """.trimIndent(),
        )

        db.query("SELECT id FROM `transaction` WHERE from_account_id = 'acc-2'").use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
            cursor.moveToFirst()
            assertThat(cursor.getString(0)).isEqualTo("txn-3")
        }
        db.close()
    }

    // endregion

    // region helpers

    private fun insertSampleData(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO category
                (id, name, type, icon_background_color, icon_name, updated_on, created_on,
                 default_category_key, custom_image_path)
            VALUES ('cat-1', 'Food', 0, '#43A546', 'restaurant', 1000, 1000, NULL, NULL)
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO account
                (id, name, type, icon_background_color, icon_name, amount, credit_limit,
                 sequence, created_on, updated_on, custom_image_path)
            VALUES
                ('acc-1', 'Checking', 0, '#FFFFFF', 'account_balance', 1000.0, 0.0, 1, 1000, 1000, NULL),
                ('acc-2', 'Savings', 0, '#000000', 'savings', 2000.0, 0.0, 2, 1000, 1000, NULL)
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `transaction`
                (id, notes, category_id, from_account_id, type, amount, image_path,
                 created_on, updated_on, to_account_id)
            VALUES
                ('txn-1', 'Lunch', 'cat-1', 'acc-1', 0, 100.5, '', 1000, 1000, NULL),
                ('txn-2', 'Move to savings', 'cat-1', 'acc-1', 2, 50.0, '', 2000, 2000, 'acc-2')
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO transaction_attachment (id, transaction_id, image_path, created_on)
            VALUES ('att-1', 'txn-1', '/files/transaction_attachments/a.jpg', 1000)
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO recurring_transaction
                (id, notes, category_id, from_account_id, to_account_id, type, amount,
                 frequency, start_date, next_occurrence, next_due_date, created_on, updated_on)
            VALUES ('rec-1', 'Rent', 'cat-1', 'acc-1', NULL, 0, 500.0,
                    'MONTHLY', 1000, 1, 5000, 1000, 1000)
            """.trimIndent(),
        )
    }

    private fun indexNames(db: SupportSQLiteDatabase, table: String): List<String> = db.query("PRAGMA index_list(`$table`)").use { cursor ->
        buildList {
            while (cursor.moveToNext()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                // Skip SQLite's own primary-key index.
                if (!name.startsWith("sqlite_autoindex")) add(name)
            }
        }
    }

    private fun queryPlan(db: SupportSQLiteDatabase, sql: String): String = db.query("EXPLAIN QUERY PLAN $sql").use { cursor ->
        buildString {
            while (cursor.moveToNext()) append(cursor.getString(cursor.getColumnIndexOrThrow("detail")))
        }
    }

    private fun count(db: SupportSQLiteDatabase, table: String): Int = db.query("SELECT COUNT(*) FROM `$table`").use { cursor ->
        cursor.moveToFirst()
        cursor.getInt(0)
    }

    // endregion
}
