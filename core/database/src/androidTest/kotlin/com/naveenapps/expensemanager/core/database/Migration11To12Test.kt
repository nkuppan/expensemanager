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
class Migration11To12Test {

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

    @Test
    @Throws(IOException::class)
    fun existingAccountsAreUntouchedAndTheLedgerTablesWork() {
        helper.createDatabase(11).apply {
            execSQL(
                """
                INSERT INTO account
                    (id, name, type, icon_background_color, icon_name, amount, credit_limit, sequence,
                     created_on, updated_on)
                VALUES ('cash', 'Cash', 0, '#FFFFFF', 'account_balance', 900.0, 0.0, 0, 1000, 1000)
                """.trimIndent(),
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(12, listOf(MIGRATION_11_12))
        db.execSQL("PRAGMA foreign_keys = ON")

        db.query("SELECT amount FROM account WHERE id = 'cash'").use { cursor ->
            cursor.moveToFirst()
            assertThat(cursor.getDouble(0)).isEqualTo(900.0)
        }

        db.execSQL("INSERT INTO debt_person (id, name, created_on, updated_on) VALUES ('ravi', 'Ravi', 1, 1)")
        db.execSQL(
            """
            INSERT INTO debt_entry
                (id, person_id, kind, amount_milli, account_id, date, due_date, note, created_on, updated_on)
            VALUES ('e1', 'ravi', 'LENT', 2000000, 'cash', 1, NULL, '', 1, 1)
            """.trimIndent(),
        )

        // Deleting the account keeps the entry and only clears the link.
        db.execSQL("DELETE FROM account WHERE id = 'cash'")
        db.query("SELECT account_id, amount_milli FROM debt_entry WHERE id = 'e1'").use { cursor ->
            cursor.moveToFirst()
            assertThat(cursor.isNull(0)).isTrue()
            assertThat(cursor.getLong(1)).isEqualTo(2_000_000L)
        }

        // Deleting the person deletes their entries.
        db.execSQL("DELETE FROM debt_person WHERE id = 'ravi'")
        db.query("SELECT COUNT(*) FROM debt_entry").use { cursor ->
            cursor.moveToFirst()
            assertThat(cursor.getInt(0)).isEqualTo(0)
        }
        db.close()
    }
}
