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

@RunWith(AndroidJUnit4::class)
class Migration8To9Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpenseManagerDatabase::class.java,
    )

    // region schema shape

    @Test
    @Throws(IOException::class)
    fun afterMigration_recurringTransactionTableExists() {
        helper.createDatabase(TEST_DB, 8).close()

        // validateDroppedTables = true also checks the new table (columns, FKs, indices)
        // against 9.json, so any drift between MIGRATION_8_9 and the entity fails here.
        val db = helper.runMigrationsAndValidate(TEST_DB, 9, true, MIGRATION_8_9)

        val cursor = db.query("PRAGMA table_info(recurring_transaction)")
        val columnNames = buildList {
            while (cursor.moveToNext()) {
                add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
        }
        cursor.close()
        db.close()

        assertThat(columnNames).containsExactly(
            "id",
            "notes",
            "category_id",
            "from_account_id",
            "to_account_id",
            "type",
            "amount",
            "frequency",
            "start_date",
            "next_occurrence",
            "next_due_date",
            "created_on",
            "updated_on",
        )
    }

    @Test
    @Throws(IOException::class)
    fun afterMigration_recurringTransactionIndicesExist() {
        helper.createDatabase(TEST_DB, 8).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 9, true, MIGRATION_8_9)

        val cursor = db.query("PRAGMA index_list(recurring_transaction)")
        val indexNames = buildList {
            while (cursor.moveToNext()) {
                add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
        }
        cursor.close()
        db.close()

        assertThat(indexNames).containsAtLeast(
            "index_recurring_transaction_from_account_id",
            "index_recurring_transaction_to_account_id",
            "index_recurring_transaction_category_id",
            "index_recurring_transaction_next_due_date",
        )
    }

    @Test
    @Throws(IOException::class)
    fun afterMigration_recurringTransactionTableStartsEmpty() {
        val db8 = helper.createDatabase(TEST_DB, 8)
        insertCategoryAndAccounts(db8)
        db8.close()

        val db9 = helper.runMigrationsAndValidate(TEST_DB, 9, true, MIGRATION_8_9)
        val cursor = db9.query("SELECT COUNT(*) FROM recurring_transaction")
        cursor.moveToFirst()
        assertThat(cursor.getInt(0)).isEqualTo(0)
        cursor.close()
        db9.close()
    }

    // endregion

    // region inserts and cascade delete

    @Test
    @Throws(IOException::class)
    fun afterMigration_canInsertAndReadRecurringTransaction() {
        val db8 = helper.createDatabase(TEST_DB, 8)
        insertCategoryAndAccounts(db8)
        db8.close()

        val db9 = helper.runMigrationsAndValidate(TEST_DB, 9, true, MIGRATION_8_9)
        insertRecurringTransaction(db9, id = "rec-1", toAccountId = null)

        val cursor = db9.query(
            "SELECT notes, category_id, from_account_id, to_account_id, amount, frequency, " +
                "next_occurrence, next_due_date FROM recurring_transaction WHERE id = 'rec-1'",
        )
        assertThat(cursor.moveToFirst()).isTrue()
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("notes"))).isEqualTo("Rent")
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("category_id"))).isEqualTo("cat-1")
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("from_account_id")))
            .isEqualTo("acc-1")
        assertThat(cursor.isNull(cursor.getColumnIndexOrThrow("to_account_id"))).isTrue()
        assertThat(cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))).isEqualTo(500.0)
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("frequency"))).isEqualTo("MONTHLY")
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow("next_occurrence"))).isEqualTo(1)
        assertThat(cursor.getLong(cursor.getColumnIndexOrThrow("next_due_date"))).isEqualTo(5000L)
        cursor.close()
        db9.close()
    }

    @Test
    @Throws(IOException::class)
    fun afterMigration_deletingFromAccountCascadesToRecurringTransactions() {
        val db9 = migrateWithOneRecurringTransaction(toAccountId = null)

        db9.execSQL("DELETE FROM account WHERE id = 'acc-1'")

        assertThat(countRecurring(db9)).isEqualTo(0)
        db9.close()
    }

    @Test
    @Throws(IOException::class)
    fun afterMigration_deletingToAccountCascadesToRecurringTransactions() {
        val db9 = migrateWithOneRecurringTransaction(toAccountId = "acc-2")

        db9.execSQL("DELETE FROM account WHERE id = 'acc-2'")

        assertThat(countRecurring(db9)).isEqualTo(0)
        db9.close()
    }

    @Test
    @Throws(IOException::class)
    fun afterMigration_deletingCategoryCascadesToRecurringTransactions() {
        val db9 = migrateWithOneRecurringTransaction(toAccountId = null)

        db9.execSQL("DELETE FROM category WHERE id = 'cat-1'")

        assertThat(countRecurring(db9)).isEqualTo(0)
        db9.close()
    }

    // endregion

    // region other tables unaffected

    @Test
    @Throws(IOException::class)
    fun afterMigration_otherTablesAreUntouched() {
        val db8 = helper.createDatabase(TEST_DB, 8)
        insertCategoryAndAccounts(db8)
        db8.execSQL(
            """
            INSERT INTO `transaction`
                (id, notes, category_id, from_account_id, type, amount, image_path,
                 created_on, updated_on, to_account_id)
            VALUES
                ('txn-1', 'Lunch', 'cat-1', 'acc-1', 0, 100.0, '', 1000, 1000, NULL)
            """.trimIndent(),
        )
        db8.execSQL(
            """
            INSERT INTO transaction_attachment (id, transaction_id, image_path, created_on)
            VALUES ('att-1', 'txn-1', '/tmp/receipt.jpg', 1000)
            """.trimIndent(),
        )
        db8.close()

        val db9 = helper.runMigrationsAndValidate(TEST_DB, 9, true, MIGRATION_8_9)

        val category = db9.query("SELECT id, name FROM category")
        assertThat(category.count).isEqualTo(1)
        category.moveToFirst()
        assertThat(category.getString(category.getColumnIndexOrThrow("name"))).isEqualTo("Food")
        category.close()

        val account = db9.query("SELECT id, name, amount FROM account ORDER BY id")
        assertThat(account.count).isEqualTo(2)
        account.moveToFirst()
        assertThat(account.getString(account.getColumnIndexOrThrow("name"))).isEqualTo("Checking")
        assertThat(account.getDouble(account.getColumnIndexOrThrow("amount"))).isEqualTo(1000.0)
        account.close()

        val transaction = db9.query("SELECT id, notes, amount FROM `transaction`")
        assertThat(transaction.count).isEqualTo(1)
        transaction.moveToFirst()
        assertThat(transaction.getString(transaction.getColumnIndexOrThrow("notes")))
            .isEqualTo("Lunch")
        assertThat(transaction.getDouble(transaction.getColumnIndexOrThrow("amount")))
            .isEqualTo(100.0)
        transaction.close()

        val attachment = db9.query("SELECT image_path FROM transaction_attachment")
        assertThat(attachment.count).isEqualTo(1)
        attachment.close()

        db9.close()
    }

    // endregion

    // region helpers

    private fun migrateWithOneRecurringTransaction(toAccountId: String?): SupportSQLiteDatabase {
        val db8 = helper.createDatabase(TEST_DB, 8)
        insertCategoryAndAccounts(db8)
        db8.close()

        val db9 = helper.runMigrationsAndValidate(TEST_DB, 9, true, MIGRATION_8_9)
        // SQLite leaves FK enforcement off per connection unless asked; Room turns it on for
        // real app connections, so turn it on here to test the cascade the app relies on.
        db9.execSQL("PRAGMA foreign_keys = ON")
        insertRecurringTransaction(db9, id = "rec-1", toAccountId = toAccountId)
        assertThat(countRecurring(db9)).isEqualTo(1)
        return db9
    }

    private fun insertCategoryAndAccounts(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO category
                (id, name, type, icon_background_color, icon_name, updated_on, created_on)
            VALUES
                ('cat-1', 'Food', 0, '#43A546', 'ic_calendar', 1000, 1000)
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO account
                (id, name, type, icon_background_color, icon_name, amount, credit_limit,
                 sequence, created_on, updated_on)
            VALUES
                ('acc-1', 'Checking', 0, '#FFFFFF', 'ic_bank', 1000.0, 0.0, 1, 1000, 2000),
                ('acc-2', 'Savings', 0, '#000000', 'ic_bank', 2000.0, 0.0, 2, 1000, 2000)
            """.trimIndent(),
        )
    }

    private fun insertRecurringTransaction(
        db: SupportSQLiteDatabase,
        id: String,
        toAccountId: String?,
    ) {
        db.execSQL(
            """
            INSERT INTO recurring_transaction
                (id, notes, category_id, from_account_id, to_account_id, type, amount,
                 frequency, start_date, next_occurrence, next_due_date, created_on, updated_on)
            VALUES
                (?, 'Rent', 'cat-1', 'acc-1', ?, 0, 500.0,
                 'MONTHLY', 1000, 1, 5000, 1000, 1000)
            """.trimIndent(),
            arrayOf<Any?>(id, toAccountId),
        )
    }

    private fun countRecurring(db: SupportSQLiteDatabase): Int {
        val cursor = db.query("SELECT COUNT(*) FROM recurring_transaction")
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count
    }

    // endregion
}
