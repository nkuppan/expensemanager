package com.naveenapps.expensemanager.core.database

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import java.io.IOException
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB = "migration_1_2_test"

/**
 * There is no exported 1.json, so MigrationTestHelper can't create a version-1 database. These
 * tests write one by hand (SQLite user_version = 1) in the layouts a v1 install may have, then
 * let the helper migrate it to 2 and validate against 2.json.
 */
@RunWith(AndroidJUnit4::class)
class Migration1To2Test {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpenseManagerDatabase::class.java,
    )

    @Before
    @After
    fun deleteTestDatabase() {
        context.deleteDatabase(TEST_DB)
    }

    private fun createVersion1Database(vararg statements: String) {
        val file = context.getDatabasePath(TEST_DB).apply { parentFile?.mkdirs() }
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            statements.forEach { db.execSQL(it) }
            db.version = 1
        }
    }

    // region same tables as v2 (the most likely v1 shape)

    private val v2LikeTables = arrayOf(
        "CREATE TABLE category (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, type INTEGER NOT NULL, " +
            "icon_background_color TEXT NOT NULL, icon_name TEXT NOT NULL, updated_on INTEGER NOT NULL, " +
            "created_on INTEGER NOT NULL)",
        "CREATE TABLE account (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, type INTEGER NOT NULL, " +
            "icon_background_color TEXT NOT NULL, icon_name TEXT NOT NULL, amount REAL NOT NULL, " +
            "created_on INTEGER NOT NULL, updated_on INTEGER NOT NULL)",
        "CREATE TABLE `transaction` (id TEXT NOT NULL PRIMARY KEY, notes TEXT NOT NULL, " +
            "category_id TEXT NOT NULL, from_account_id TEXT NOT NULL, type INTEGER NOT NULL, " +
            "amount REAL NOT NULL, image_path TEXT NOT NULL, created_on INTEGER NOT NULL, " +
            "updated_on INTEGER NOT NULL, to_account_id TEXT)",
    )

    @Test
    @Throws(IOException::class)
    fun v2LikeData_isCopiedAndMissingColumnsAndTablesAreAdded() {
        createVersion1Database(
            *v2LikeTables,
            "INSERT INTO category VALUES ('3', 'Food', 1, '#F44336', 'restaurant', 1000, 1000)",
            "INSERT INTO account VALUES ('acc-1', 'Wallet', 0, '#FFFFFF', 'payments', 250.0, 1000, 1000)",
            "INSERT INTO account VALUES ('acc-2', 'Bank', 0, '#000000', 'account_balance', 900.0, 1000, 1000)",
            "INSERT INTO `transaction` VALUES ('t-1', 'Lunch', '3', 'acc-1', 1, 12.5, '', 2000, 2000, NULL)",
            "INSERT INTO `transaction` VALUES ('t-2', 'Top up', '3', 'acc-2', 2, 50.0, '', 3000, 3000, 'acc-1')",
        )

        // validateDroppedTables = true: every v2 table, column and FK must match 2.json exactly.
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        db.query("SELECT name, type, icon_background_color, icon_name FROM category").use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
            cursor.moveToFirst()
            assertThat(cursor.getString(0)).isEqualTo("Food")
            assertThat(cursor.getInt(1)).isEqualTo(1)
            assertThat(cursor.getString(2)).isEqualTo("#F44336")
            assertThat(cursor.getString(3)).isEqualTo("restaurant")
        }
        db.query("SELECT id, amount, credit_limit FROM account ORDER BY id").use { cursor ->
            assertThat(cursor.count).isEqualTo(2)
            cursor.moveToFirst()
            assertThat(cursor.getDouble(1)).isEqualTo(250.0)
            assertThat(cursor.getDouble(2)).isEqualTo(0.0) // added column, defaulted
        }
        db.query("SELECT id, from_account_id, to_account_id, type, amount FROM `transaction` ORDER BY id").use { cursor ->
            assertThat(cursor.count).isEqualTo(2)
            cursor.moveToFirst()
            assertThat(cursor.getString(1)).isEqualTo("acc-1")
            assertThat(cursor.getDouble(4)).isEqualTo(12.5)
            cursor.moveToNext()
            assertThat(cursor.getString(2)).isEqualTo("acc-1")
            assertThat(cursor.getInt(3)).isEqualTo(2)
        }
        assertThat(count(db, "budget")).isEqualTo(0)
        assertThat(count(db, "budget_category_relation")).isEqualTo(0)
        assertThat(tableNames(db)).containsNoneOf("category_v1_old", "account_v1_old", "transaction_v1_old")
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun orphanedRows_areRepairedInsteadOfFailingTheMigration() {
        createVersion1Database(
            *v2LikeTables,
            "INSERT INTO category VALUES ('3', 'Food', 1, '#F44336', 'restaurant', 1000, 1000)",
            "INSERT INTO account VALUES ('acc-1', 'Wallet', 0, '#FFFFFF', 'payments', 250.0, 1000, 1000)",
            // Account that no longer exists -> moved to the fallback account.
            "INSERT INTO `transaction` VALUES ('t-1', 'Lunch', '3', 'gone', 1, 12.5, '', 2000, 2000, NULL)",
            // Transfer to a deleted account -> plain transaction.
            "INSERT INTO `transaction` VALUES ('t-2', 'Move', '3', 'acc-1', 2, 5.0, '', 2000, 2000, 'gone')",
            // Category that no longer exists -> dropped (the cascade would have removed it).
            "INSERT INTO `transaction` VALUES ('t-3', 'Lost', 'missing', 'acc-1', 1, 1.0, '', 2000, 2000, NULL)",
        )

        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        db.query("SELECT id, from_account_id, to_account_id FROM `transaction` ORDER BY id").use { cursor ->
            assertThat(cursor.count).isEqualTo(2)
            cursor.moveToFirst()
            assertThat(cursor.getString(1)).isEqualTo(FALLBACK_ACCOUNT_ID)
            cursor.moveToNext()
            assertThat(cursor.isNull(2)).isTrue()
        }
        db.query("SELECT name FROM account WHERE id = '$FALLBACK_ACCOUNT_ID'").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("Cash")
        }
        assertForeignKeysHold(db)
        db.close()
    }

    // endregion

    // region original app layout (integer ids, no accounts)

    @Test
    @Throws(IOException::class)
    fun originalAppLayout_isMappedOntoV2() {
        // The first published schema: integer ids, `background_color`, and transactions linked
        // to a category through `category_type`, with no account table at all.
        createVersion1Database(
            "CREATE TABLE category (id INTEGER NOT NULL PRIMARY KEY, name TEXT NOT NULL, " +
                "type INTEGER NOT NULL, background_color TEXT NOT NULL, created_on INTEGER NOT NULL, " +
                "is_favorite INTEGER NOT NULL, updated_on INTEGER NOT NULL)",
            "CREATE TABLE `transaction` (id INTEGER NOT NULL PRIMARY KEY, notes TEXT NOT NULL, " +
                "category_type INTEGER NOT NULL, amount REAL NOT NULL, image_path TEXT NOT NULL, " +
                "created_on INTEGER NOT NULL, updated_on INTEGER NOT NULL, " +
                "FOREIGN KEY(category_type) REFERENCES category(id) ON DELETE CASCADE)",
            "INSERT INTO category VALUES (7, 'Travel', 1, '#3F51B5', 1000, 0, 1000)",
            "INSERT INTO `transaction` VALUES (1, 'Train ticket', 7, 30.0, '', 2000, 2000)",
        )

        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        db.query("SELECT id, name, icon_background_color, icon_name FROM category").use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
            cursor.moveToFirst()
            assertThat(cursor.getString(0)).isEqualTo("7")
            assertThat(cursor.getString(1)).isEqualTo("Travel")
            assertThat(cursor.getString(2)).isEqualTo("#3F51B5")
            assertThat(cursor.getString(3)).isEqualTo("wallet")
        }
        db.query("SELECT notes, category_id, from_account_id, type, amount FROM `transaction`").use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
            cursor.moveToFirst()
            assertThat(cursor.getString(0)).isEqualTo("Train ticket")
            assertThat(cursor.getString(1)).isEqualTo("7")
            assertThat(cursor.getString(2)).isEqualTo(FALLBACK_ACCOUNT_ID)
            assertThat(cursor.getInt(3)).isEqualTo(1) // EXPENSE
            assertThat(cursor.getDouble(4)).isEqualTo(30.0)
        }
        assertForeignKeysHold(db)
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun emptyVersion1Database_migrates() {
        createVersion1Database("CREATE TABLE category (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL)")

        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        assertThat(count(db, "category")).isEqualTo(0)
        assertThat(count(db, "account")).isEqualTo(0)
        db.close()
    }

    // endregion

    // region full upgrade, the way the app opens the file

    @Test
    fun version1Database_opensWithTheAppsRoomConfiguration() {
        createVersion1Database(
            *v2LikeTables,
            "INSERT INTO category VALUES ('3', 'Food', 1, '#F44336', 'restaurant', 1000, 1000)",
            "INSERT INTO account VALUES ('acc-1', 'Wallet', 0, '#FFFFFF', 'payments', 250.0, 1000, 1000)",
            "INSERT INTO `transaction` VALUES ('t-1', 'Lunch', '3', 'acc-1', 1, 12.5, '', 2000, 2000, NULL)",
        )

        // This used to throw "A migration from 1 to 2 was required but not found".
        val database = Room.databaseBuilder(context, ExpenseManagerDatabase::class.java, TEST_DB)
            .addMigrations(*ALL_MIGRATIONS)
            .build()
        try {
            val db = database.openHelper.writableDatabase
            assertThat(db.version).isEqualTo(ALL_MIGRATIONS.last().endVersion)
            assertThat(count(db, "transaction")).isEqualTo(1)
            db.query("SELECT default_category_key FROM category WHERE id = '3'").use { cursor ->
                cursor.moveToFirst()
                assertThat(cursor.getString(0)).isEqualTo("food") // MIGRATION_4_5 still applies
            }
        } finally {
            database.close()
        }
    }

    // endregion

    // region helpers

    private fun count(db: SupportSQLiteDatabase, table: String): Int = db.query("SELECT COUNT(*) FROM `$table`").use { cursor ->
        cursor.moveToFirst()
        cursor.getInt(0)
    }

    private fun tableNames(db: SupportSQLiteDatabase): List<String> = db.query("SELECT name FROM sqlite_master WHERE type = 'table'").use { cursor ->
        buildList { while (cursor.moveToNext()) add(cursor.getString(0)) }
    }

    private fun assertForeignKeysHold(db: SupportSQLiteDatabase) {
        db.query("PRAGMA foreign_key_check").use { cursor ->
            assertThat(cursor.count).isEqualTo(0)
        }
    }

    // endregion
}
