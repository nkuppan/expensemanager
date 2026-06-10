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
