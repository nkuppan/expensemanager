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
class Migration11To12Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpenseManagerDatabase::class.java,
    )

    private val migration = Migration11To12 { "SYP" }

    private fun SupportSQLiteDatabase.columns(table: String): List<String> {
        val cursor = query("PRAGMA table_info(`$table`)")
        return buildList {
            while (cursor.moveToNext()) add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
        }.also { cursor.close() }
    }

    private fun SupportSQLiteDatabase.strings(sql: String): List<String?> {
        val cursor = query(sql)
        return buildList {
            while (cursor.moveToNext()) add(cursor.getString(0))
        }.also { cursor.close() }
    }

    private fun seedV11(db: SupportSQLiteDatabase) {
        db.execSQL(
            "INSERT INTO category (id, name, type, icon_background_color, icon_name, updated_on, " +
                "created_on) VALUES ('c1', 'Food', 1, '#FFFFFF', 'restaurant', 0, 0)",
        )
        db.execSQL(
            "INSERT INTO account (id, name, type, icon_background_color, icon_name, amount, " +
                "credit_limit, sequence, created_on, updated_on) " +
                "VALUES ('a1', 'Cash', 0, '#FFFFFF', 'savings', 50.0, 0.0, 0, 0, 0), " +
                "('a2', 'Bank', 0, '#FFFFFF', 'account_balance', 100.0, 0.0, 1, 0, 0)",
        )
        db.execSQL(
            "INSERT INTO `transaction` (id, notes, category_id, from_account_id, type, amount, " +
                "image_path, created_on, updated_on, to_account_id) VALUES " +
                "('t1', '', 'c1', 'a1', 1, 10.0, '', 0, 0, NULL), " +
                "('t2', '', 'c1', 'a1', 2, 20.0, '', 0, 0, 'a2')",
        )
        db.execSQL(
            "INSERT INTO transaction_attachment (id, transaction_id, image_path, created_on) " +
                "VALUES ('att1', 't1', '/receipt.jpg', 0)",
        )
        db.execSQL(
            "INSERT INTO budget (id, selected_month, amount, period_type, all_accounts_selected, " +
                "all_categories_selected, created_on, updated_on) " +
                "VALUES ('b1', 'September 2026', 500.0, 0, 1, 1, 0, 0)",
        )
    }

    // region schema shape

    @Test
    @Throws(IOException::class)
    fun schemaMatchesVersion12() {
        helper.createDatabase(TEST_DB, 11).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 12, true, migration)

        assertThat(db.columns("account")).containsAtLeast("currency_code", "is_archived")
        assertThat(db.columns("transaction"))
            .containsAtLeast("from_currency_code", "to_currency_code", "rate_snapshot_id")
        assertThat(db.columns("budget")).contains("currency_code")
        assertThat(db.columns("exchange_rate_snapshot")).containsExactly(
            "id",
            "source",
            "provider",
            "base_code",
            "quote_code",
            "rate",
            "captured_at",
        )
        assertThat(db.columns("exchange_rates"))
            .containsExactly("provider", "base_code", "target_code", "rate", "fetched_at")
        assertThat(db.columns("static_exchange_rate"))
            .containsExactly("base_code", "quote_code", "buy", "sell", "updated_on")
        db.close()
    }

    // endregion

    // region backfill: every legacy row gets the one legacy currency, none is left blank

    @Test
    @Throws(IOException::class)
    fun legacyRowsAllGetTheLegacyCurrency() {
        helper.createDatabase(TEST_DB, 11).apply { seedV11(this) }.close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 12, true, migration)

        assertThat(db.strings("SELECT currency_code FROM account")).containsExactly("SYP", "SYP")
        assertThat(db.strings("SELECT from_currency_code FROM `transaction`"))
            .containsExactly("SYP", "SYP")
        assertThat(db.strings("SELECT to_currency_code FROM `transaction`"))
            .containsExactly("SYP", "SYP")
        assertThat(db.strings("SELECT rate_snapshot_id FROM `transaction`"))
            .containsExactly(null, null)
        assertThat(db.strings("SELECT currency_code FROM budget")).containsExactly("SYP")
        assertThat(db.strings("SELECT is_archived FROM account")).containsExactly("0", "0")
        db.close()
    }

    // endregion

    // region other data untouched (tables are altered in place, never rebuilt)

    @Test
    @Throws(IOException::class)
    fun existingDataAndCascadeChildrenSurvive() {
        helper.createDatabase(TEST_DB, 11).apply { seedV11(this) }.close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 12, true, migration)

        assertThat(db.strings("SELECT CAST(amount * 100 AS INTEGER) FROM account ORDER BY id"))
            .containsExactly("5000", "10000")
        assertThat(db.strings("SELECT id FROM `transaction` ORDER BY id")).containsExactly("t1", "t2")
        assertThat(db.strings("SELECT id FROM transaction_attachment")).containsExactly("att1")
        assertThat(db.strings("SELECT id FROM category")).containsExactly("c1")
        db.close()
    }

    // endregion
}
