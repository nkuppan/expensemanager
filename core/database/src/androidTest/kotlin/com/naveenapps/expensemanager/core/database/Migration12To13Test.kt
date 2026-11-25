package com.naveenapps.expensemanager.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import java.io.IOException
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB = "migration_12_13_test"

@RunWith(AndroidJUnit4::class)
class Migration12To13Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpenseManagerDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun existingRowsSurviveAndTheRefundLinkBehavesLikeAForeignKey() {
        helper.createDatabase(TEST_DB, 12).apply {
            execSQL(
                """
                INSERT INTO category (id, name, type, icon_background_color, icon_name, updated_on, created_on)
                VALUES ('food', 'Food', 0, '#43A546', 'restaurant', 1000, 1000)
                """.trimIndent(),
            )
            execSQL(
                """
                INSERT INTO account
                    (id, name, type, icon_background_color, icon_name, amount, credit_limit, sequence,
                     created_on, updated_on)
                VALUES ('cash', 'Cash', 0, '#FFFFFF', 'account_balance', 900.0, 0.0, 0, 1000, 1000)
                """.trimIndent(),
            )
            execSQL(
                """
                INSERT INTO `transaction`
                    (id, notes, category_id, from_account_id, type, amount, image_path,
                     created_on, updated_on, to_account_id)
                VALUES ('txn-1', 'Groceries', 'food', 'cash', 1, 250.0, 'receipt.jpg', 1000, 1000, NULL)
                """.trimIndent(),
            )
            execSQL(
                """
                INSERT INTO transaction_attachment (id, transaction_id, image_path, created_on)
                VALUES ('att-1', 'txn-1', 'receipt.jpg', 1000)
                """.trimIndent(),
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 13, true, MIGRATION_12_13)
        db.execSQL("PRAGMA foreign_keys = ON")

        // Every value of the old table is still there, and the new link starts NULL.
        db.query(
            "SELECT notes, amount, image_path, original_transaction_id " +
                "FROM `transaction` WHERE id = 'txn-1'",
        ).use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("Groceries")
            assertThat(cursor.getDouble(1)).isEqualTo(250.0)
            assertThat(cursor.getString(2)).isEqualTo("receipt.jpg")
            assertThat(cursor.isNull(3)).isTrue()
        }

        // Recreating the table must not cascade-delete the rows that pointed at it.
        db.query("SELECT transaction_id FROM transaction_attachment WHERE id = 'att-1'").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("txn-1")
        }

        // A refund can point at the transaction it gives back...
        db.execSQL(
            """
            INSERT INTO `transaction`
                (id, notes, category_id, from_account_id, type, amount, image_path,
                 created_on, updated_on, to_account_id, original_transaction_id)
            VALUES ('txn-2', 'Refund', 'food', 'cash', 3, 250.0, '', 2000, 2000, NULL, 'txn-1')
            """.trimIndent(),
        )

        // ...and deleting the original only clears that link (ON DELETE SET NULL).
        db.execSQL("DELETE FROM `transaction` WHERE id = 'txn-1'")
        db.query("SELECT id, original_transaction_id FROM `transaction` ORDER BY id").use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
            cursor.moveToFirst()
            assertThat(cursor.getString(0)).isEqualTo("txn-2")
            assertThat(cursor.isNull(1)).isTrue()
        }
        db.close()
    }
}
