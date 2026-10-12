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
class Migration9To10Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpenseManagerDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun afterMigration_transactionIndicesExistAndMatchSchema() {
        helper.createDatabase(TEST_DB, 9).close()

        // validateDroppedTables = true compares every table, indices included, with 10.json.
        val db = helper.runMigrationsAndValidate(TEST_DB, 10, true, MIGRATION_9_10)

        val transactionIndices = db.query("PRAGMA index_list(`transaction`)").use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
        }
        val attachmentIndices = db.query("PRAGMA index_list(`transaction_attachment`)").use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
        }
        db.close()

        assertThat(transactionIndices).containsAtLeast(
            "index_transaction_created_on",
            "index_transaction_from_account_id",
            "index_transaction_to_account_id",
            "index_transaction_category_id",
        )
        assertThat(attachmentIndices).contains("index_transaction_attachment_transaction_id")
    }
}
