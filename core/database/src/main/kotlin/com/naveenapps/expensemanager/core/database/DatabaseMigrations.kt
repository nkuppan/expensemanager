package com.naveenapps.expensemanager.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `account` ADD COLUMN `sequence` INTEGER NOT NULL DEFAULT 0")
    }
}

internal val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `budget_new` (" +
                "`id` TEXT NOT NULL, " +
                "`selected_month` TEXT NOT NULL, " +
                "`amount` REAL NOT NULL, " +
                "`all_accounts_selected` INTEGER NOT NULL, " +
                "`all_categories_selected` INTEGER NOT NULL, " +
                "`created_on` INTEGER NOT NULL, " +
                "`updated_on` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "INSERT INTO `budget_new` " +
                "(`id`, `selected_month`, `amount`, `all_accounts_selected`, `all_categories_selected`, `created_on`, `updated_on`) " +
                "SELECT `id`, `selected_month`, `amount`, `all_accounts_selected`, `all_categories_selected`, `created_on`, `updated_on` " +
                "FROM `budget`"
        )
        db.execSQL("DROP TABLE `budget`")
        db.execSQL("ALTER TABLE `budget_new` RENAME TO `budget`")
    }
}

/**
 * Adds a nullable `default_category_key` column used to mark which rows are the app's
 * built-in seeded categories (as opposed to user-created ones), so their display name can be
 * localized via string resources instead of the raw stored `name`.
 *
 * For existing installs, the 11 categories seeded by `PreloadDatabaseInitializer.BASE_CATEGORY_LIST`
 * always got the deterministic ids "1".."11" on first launch (that list is only ever inserted into
 * an empty table). We backfill the key for those ids, but only when `name` still matches the
 * original seeded English name — if a user has since renamed one of these categories, we leave
 * `default_category_key` null so their custom name is preserved instead of being overwritten by a
 * translation the user never asked for.
 */
internal val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `category` ADD COLUMN `default_category_key` TEXT DEFAULT NULL")

        val defaults = listOf(
            Triple("1", "Clothing", "clothing"),
            Triple("2", "Entertainment", "entertainment"),
            Triple("3", "Food", "food"),
            Triple("4", "Health", "health"),
            Triple("5", "Leisure", "leisure"),
            Triple("6", "Shopping", "shopping"),
            Triple("7", "Transportation", "transportation"),
            Triple("8", "Utilities", "utilities"),
            Triple("9", "Salary", "salary"),
            Triple("10", "Gift", "gift"),
            Triple("11", "Coupons", "coupons"),
        )

        defaults.forEach { (id, originalName, key) ->
            db.execSQL(
                "UPDATE `category` SET `default_category_key` = ? " +
                    "WHERE `id` = ? AND `name` = ?",
                arrayOf(key, id, originalName),
            )
        }
    }
}
