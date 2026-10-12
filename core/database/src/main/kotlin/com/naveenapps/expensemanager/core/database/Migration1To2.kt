package com.naveenapps.expensemanager.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Version 1 of `expense_manager_database.db` predates this repository: its schema was never
 * exported (schemas start at 2.json) and no 1→2 migration was ever shipped, so anyone updating
 * from such an install crashed on every launch with "A migration from 1 to 2 was required but
 * not found".
 *
 * Because the exact v1 layout is unknown, this migration doesn't assume it. It rebuilds every
 * table with the exact v2 definition (matching 2.json, so the later migrations and Room's schema
 * validation work) and copies over whatever v1 data it can find:
 * - columns are matched by name, plus a few known older names (e.g. `background_color`,
 *   `category_type`, `account_id`) used by the original app;
 * - missing or NULL values get safe defaults (unknown icon names already fall back to a
 *   default drawable in the UI);
 * - transactions without a usable account are moved to a fallback "Cash" account rather than
 *   dropped; rows whose category no longer exists are dropped, as the cascade would have done.
 * Old tables are removed at the end. Anything unexpected only loses that data, never the launch.
 */
internal val MIGRATION_1_2 = object : Migration(1, 2) {

    override fun migrate(db: SupportSQLiteDatabase) {
        // Room runs migrations inside a transaction, where foreign_keys can't be switched off;
        // deferring the checks to commit lets tables be rebuilt in any order.
        db.execSQL("PRAGMA defer_foreign_keys = ON")

        val oldColumns = V2_TABLES.associate { table ->
            table.name to columnsOf(db, table.name).also { columns ->
                if (columns.isNotEmpty()) db.execSQL("ALTER TABLE `${table.name}` RENAME TO `${table.name}$OLD_SUFFIX`")
            }
        }

        V2_TABLES.forEach { table ->
            db.execSQL(table.createSql)
            val columns = oldColumns[table.name].orEmpty()
            if (columns.isNotEmpty()) copyRows(db, table, columns)
        }

        repairTransactions(db)
        removeOrphans(db)

        V2_TABLES.forEach { table -> db.execSQL("DROP TABLE IF EXISTS `${table.name}$OLD_SUFFIX`") }
    }

    private fun copyRows(db: SupportSQLiteDatabase, table: V2Table, oldColumns: Set<String>) {
        val selects = table.columns.joinToString(", ") { column ->
            val source = column.names.firstOrNull { it in oldColumns }
            when {
                source == null -> column.default
                column.default == "NULL" -> "`$source`"
                else -> "COALESCE(`$source`, ${column.default})"
            }
        }
        val targets = table.columns.joinToString(", ") { "`${it.name}`" }
        db.execSQL(
            "INSERT OR IGNORE INTO `${table.name}` ($targets) " +
                "SELECT $selects FROM `${table.name}$OLD_SUFFIX`",
        )
    }

    /** Transactions need an existing account; give account-less ones a fallback account. */
    private fun repairTransactions(db: SupportSQLiteDatabase) {
        val needsFallback = db.query(
            "SELECT COUNT(*) FROM `transaction` WHERE from_account_id NOT IN (SELECT id FROM account)",
        ).use { it.moveToFirst() && it.getInt(0) > 0 }
        if (needsFallback) {
            db.execSQL(
                "INSERT OR IGNORE INTO account (id, name, type, icon_background_color, icon_name, " +
                    "amount, credit_limit, created_on, updated_on) VALUES " +
                    "('$FALLBACK_ACCOUNT_ID', 'Cash', 0, '#43A546', 'account_balance', 0, 0, $NOW, $NOW)",
            )
            db.execSQL(
                "UPDATE `transaction` SET from_account_id = '$FALLBACK_ACCOUNT_ID' " +
                    "WHERE from_account_id NOT IN (SELECT id FROM account)",
            )
        }
        // A transfer to an account that's gone becomes a plain transaction.
        db.execSQL(
            "UPDATE `transaction` SET to_account_id = NULL " +
                "WHERE to_account_id IS NOT NULL AND to_account_id NOT IN (SELECT id FROM account)",
        )
    }

    /** Rows pointing at a parent that doesn't exist would fail the deferred FK check. */
    private fun removeOrphans(db: SupportSQLiteDatabase) {
        db.execSQL("DELETE FROM `transaction` WHERE category_id NOT IN (SELECT id FROM category)")
        db.execSQL(
            "DELETE FROM budget_category_relation WHERE budget_id NOT IN (SELECT id FROM budget) " +
                "OR category_id NOT IN (SELECT id FROM category)",
        )
        db.execSQL(
            "DELETE FROM budget_account_relation WHERE budget_id NOT IN (SELECT id FROM budget) " +
                "OR account_id NOT IN (SELECT id FROM account)",
        )
    }

    private fun columnsOf(db: SupportSQLiteDatabase, table: String): Set<String> = db.query("PRAGMA table_info(`$table`)").use { cursor ->
        buildSet {
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) add(cursor.getString(nameIndex))
        }
    }
}

internal const val FALLBACK_ACCOUNT_ID = "migrated-cash-account"
private const val OLD_SUFFIX = "_v1_old"
private const val NOW = "(CAST(strftime('%s', 'now') AS INTEGER) * 1000)"

/** [names]: the v2 column name first, then older names it may have had in v1. */
private class V2Column(val name: String, val default: String, vararg olderNames: String) {
    val names = listOf(name) + olderNames
}

private class V2Table(val name: String, val createSql: String, vararg val columns: V2Column)

// Exactly the CREATE statements from schemas/.../2.json, parents before children.
private val V2_TABLES = listOf(
    V2Table(
        "category",
        "CREATE TABLE IF NOT EXISTS `category` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, " +
            "`type` INTEGER NOT NULL, `icon_background_color` TEXT NOT NULL, `icon_name` TEXT NOT NULL, " +
            "`updated_on` INTEGER NOT NULL, `created_on` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        V2Column("id", "lower(hex(randomblob(16)))"),
        V2Column("name", "''"),
        V2Column("type", "1"),
        V2Column("icon_background_color", "'#43A546'", "background_color"),
        V2Column("icon_name", "'wallet'"),
        V2Column("updated_on", NOW),
        V2Column("created_on", NOW),
    ),
    V2Table(
        "account",
        "CREATE TABLE IF NOT EXISTS `account` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, " +
            "`type` INTEGER NOT NULL, `icon_background_color` TEXT NOT NULL, `icon_name` TEXT NOT NULL, " +
            "`amount` REAL NOT NULL, `credit_limit` REAL NOT NULL, `created_on` INTEGER NOT NULL, " +
            "`updated_on` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        V2Column("id", "lower(hex(randomblob(16)))"),
        V2Column("name", "''"),
        V2Column("type", "0"),
        V2Column("icon_background_color", "'#43A546'", "background_color"),
        V2Column("icon_name", "'account_balance'"),
        V2Column("amount", "0"),
        V2Column("credit_limit", "0"),
        V2Column("created_on", NOW),
        V2Column("updated_on", NOW),
    ),
    V2Table(
        "transaction",
        "CREATE TABLE IF NOT EXISTS `transaction` (`id` TEXT NOT NULL, `notes` TEXT NOT NULL, " +
            "`category_id` TEXT NOT NULL, `from_account_id` TEXT NOT NULL, `type` INTEGER NOT NULL, " +
            "`amount` REAL NOT NULL, `image_path` TEXT NOT NULL, `created_on` INTEGER NOT NULL, " +
            "`updated_on` INTEGER NOT NULL, `to_account_id` TEXT, PRIMARY KEY(`id`), " +
            "FOREIGN KEY(`from_account_id`) REFERENCES `account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , " +
            "FOREIGN KEY(`to_account_id`) REFERENCES `account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , " +
            "FOREIGN KEY(`category_id`) REFERENCES `category`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        V2Column("id", "lower(hex(randomblob(16)))"),
        V2Column("notes", "''"),
        V2Column("category_id", "''", "category_type"),
        V2Column("from_account_id", "'$FALLBACK_ACCOUNT_ID'", "account_id"),
        // EXPENSE (TransactionType ordinal 1) when the old row didn't record a type.
        V2Column("type", "1"),
        V2Column("amount", "0"),
        V2Column("image_path", "''"),
        V2Column("created_on", NOW),
        V2Column("updated_on", NOW),
        V2Column("to_account_id", "NULL"),
    ),
    V2Table(
        "budget",
        "CREATE TABLE IF NOT EXISTS `budget` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, " +
            "`icon_background_color` TEXT NOT NULL, `icon_name` TEXT NOT NULL, `selected_month` TEXT NOT NULL, " +
            "`amount` REAL NOT NULL, `all_accounts_selected` INTEGER NOT NULL, " +
            "`all_categories_selected` INTEGER NOT NULL, `created_on` INTEGER NOT NULL, " +
            "`updated_on` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        V2Column("id", "lower(hex(randomblob(16)))"),
        V2Column("name", "''"),
        V2Column("icon_background_color", "'#43A546'", "background_color"),
        V2Column("icon_name", "'wallet'"),
        V2Column("selected_month", "''"),
        V2Column("amount", "0"),
        V2Column("all_accounts_selected", "1"),
        V2Column("all_categories_selected", "1"),
        V2Column("created_on", NOW),
        V2Column("updated_on", NOW),
    ),
    V2Table(
        "budget_category_relation",
        "CREATE TABLE IF NOT EXISTS `budget_category_relation` (`id` TEXT NOT NULL, `budget_id` TEXT NOT NULL, " +
            "`category_id` TEXT NOT NULL, `created_on` INTEGER NOT NULL, `updated_on` INTEGER NOT NULL, " +
            "PRIMARY KEY(`id`), FOREIGN KEY(`budget_id`) REFERENCES `budget`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , " +
            "FOREIGN KEY(`category_id`) REFERENCES `category`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        V2Column("id", "lower(hex(randomblob(16)))"),
        V2Column("budget_id", "''"),
        V2Column("category_id", "''"),
        V2Column("created_on", NOW),
        V2Column("updated_on", NOW),
    ),
    V2Table(
        "budget_account_relation",
        "CREATE TABLE IF NOT EXISTS `budget_account_relation` (`id` TEXT NOT NULL, `budget_id` TEXT NOT NULL, " +
            "`account_id` TEXT NOT NULL, `created_on` INTEGER NOT NULL, `updated_on` INTEGER NOT NULL, " +
            "PRIMARY KEY(`id`), FOREIGN KEY(`budget_id`) REFERENCES `budget`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , " +
            "FOREIGN KEY(`account_id`) REFERENCES `account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        V2Column("id", "lower(hex(randomblob(16)))"),
        V2Column("budget_id", "''"),
        V2Column("account_id", "''"),
        V2Column("created_on", NOW),
        V2Column("updated_on", NOW),
    ),
)
