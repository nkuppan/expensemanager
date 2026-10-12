package com.naveenapps.expensemanager.core.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import java.io.IOException
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB = "migration_chain_test"

/**
 * Upgrades a version-2 database (the oldest exported schema) all the way to the
 * current version with [ALL_MIGRATIONS], the same list the app registers. Catches a missing step
 * in the chain, a migration that only works from its direct predecessor, and data lost or
 * changed anywhere along the way. Version 1 has no exported schema; see Migration1To2Test.
 */
@RunWith(AndroidJUnit4::class)
class MigrationFullChainTest {

    private val latestVersion = 10

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpenseManagerDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun version2DatabaseMigratesToLatestWithItsDataIntact() {
        helper.createDatabase(TEST_DB, 2).apply {
            // Id "3"/"Food" is one of the seeded defaults that MIGRATION_4_5 backfills.
            execSQL(
                """
                INSERT INTO category (id, name, type, icon_background_color, icon_name, updated_on, created_on)
                VALUES ('3', 'Food', 0, '#43A546', 'restaurant', 1000, 1000),
                       ('custom', 'Pets', 0, '#000000', 'pets', 1000, 1000)
                """.trimIndent(),
            )
            execSQL(
                """
                INSERT INTO account
                    (id, name, type, icon_background_color, icon_name, amount, credit_limit, created_on, updated_on)
                VALUES ('acc-1', 'Checking', 0, '#FFFFFF', 'account_balance', 900.0, 0.0, 1000, 1000),
                       ('acc-2', 'Card', 1, '#FF0000', 'credit_card', -50.0, 500.0, 1000, 1000)
                """.trimIndent(),
            )
            execSQL(
                """
                INSERT INTO `transaction`
                    (id, notes, category_id, from_account_id, type, amount, image_path,
                     created_on, updated_on, to_account_id)
                VALUES ('txn-1', 'Groceries', '3', 'acc-1', 0, 100.0, '', 1000, 1000, NULL),
                       ('txn-2', 'Card payment', '3', 'acc-1', 2, 50.0, '', 2000, 2000, 'acc-2')
                """.trimIndent(),
            )
            execSQL(
                """
                INSERT INTO budget
                    (id, name, icon_background_color, icon_name, selected_month, amount,
                     all_accounts_selected, all_categories_selected, created_on, updated_on)
                VALUES ('bud-1', 'Monthly', '#FFFFFF', 'savings', 'October 2026', 400.0, 1, 1, 1000, 1000)
                """.trimIndent(),
            )
            close()
        }

        // Validates every table, column and index against the latest exported schema.
        val db = helper.runMigrationsAndValidate(TEST_DB, latestVersion, true, *ALL_MIGRATIONS)

        db.query("SELECT id, default_category_key FROM category ORDER BY id").use { cursor ->
            assertThat(cursor.count).isEqualTo(2)
            cursor.moveToFirst()
            assertThat(cursor.getString(0)).isEqualTo("3")
            assertThat(cursor.getString(1)).isEqualTo("food")
            cursor.moveToNext()
            assertThat(cursor.getString(0)).isEqualTo("custom")
            assertThat(cursor.isNull(1)).isTrue()
        }

        db.query("SELECT id, amount, credit_limit, sequence FROM account ORDER BY id").use { cursor ->
            assertThat(cursor.count).isEqualTo(2)
            cursor.moveToFirst()
            assertThat(cursor.getDouble(1)).isEqualTo(900.0)
            cursor.moveToNext()
            assertThat(cursor.getDouble(1)).isEqualTo(-50.0)
            assertThat(cursor.getDouble(2)).isEqualTo(500.0)
            assertThat(cursor.getInt(3)).isEqualTo(0) // MIGRATION_2_3 default
        }

        db.query("SELECT id, amount, to_account_id FROM `transaction` ORDER BY id").use { cursor ->
            assertThat(cursor.count).isEqualTo(2)
            cursor.moveToFirst()
            assertThat(cursor.getDouble(1)).isEqualTo(100.0)
            cursor.moveToNext()
            assertThat(cursor.getString(2)).isEqualTo("acc-2")
        }

        db.query("SELECT selected_month, amount, period_type FROM budget").use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
            cursor.moveToFirst()
            assertThat(cursor.getString(0)).isEqualTo("October 2026")
            assertThat(cursor.getDouble(1)).isEqualTo(400.0)
            assertThat(cursor.getInt(2)).isEqualTo(0) // MONTHLY, MIGRATION_5_6 default
        }

        db.query("SELECT COUNT(*) FROM recurring_transaction").use { cursor ->
            cursor.moveToFirst()
            assertThat(cursor.getInt(0)).isEqualTo(0)
        }
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migratedDatabaseOpensWithTheAppsRoomConfiguration() {
        helper.createDatabase(TEST_DB, 2).close()
        helper.runMigrationsAndValidate(TEST_DB, latestVersion, true, *ALL_MIGRATIONS).close()

        // Open the migrated file the way the app does; Room re-validates the schema on open.
        val database = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            ExpenseManagerDatabase::class.java,
            TEST_DB,
        ).addMigrations(*ALL_MIGRATIONS).build()
        try {
            assertThat(database.openHelper.writableDatabase.version).isEqualTo(latestVersion)
        } finally {
            database.close()
        }
    }

    @Test
    fun everyVersionStepHasExactlyOneMigration() {
        val steps = ALL_MIGRATIONS.map { it.startVersion to it.endVersion }
        assertThat(steps).isEqualTo((1 until latestVersion).map { it to it + 1 })
    }
}
