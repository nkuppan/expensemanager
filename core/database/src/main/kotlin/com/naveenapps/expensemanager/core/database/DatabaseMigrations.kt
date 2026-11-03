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
                "PRIMARY KEY(`id`))",
        )
        db.execSQL(
            "INSERT INTO `budget_new` " +
                "(`id`, `selected_month`, `amount`, `all_accounts_selected`, `all_categories_selected`, `created_on`, `updated_on`) " +
                "SELECT `id`, `selected_month`, `amount`, `all_accounts_selected`, `all_categories_selected`, `created_on`, `updated_on` " +
                "FROM `budget`",
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

/**
 * Adds a `period_type` column to `budget` so a budget can cover either a single month or an
 * entire year (see `core.model.BudgetPeriod`). `BudgetPeriod.MONTHLY` is ordinal 0, matching the
 * column's `DEFAULT 0`, so every budget that existed before this migration — all of which were
 * implicitly monthly — is correctly backfilled without needing a separate UPDATE statement.
 */
internal val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `budget` ADD COLUMN `period_type` INTEGER NOT NULL DEFAULT 0")
    }
}

/**
 * Adds a nullable `custom_image_path` column to both `category` and `account`, holding the
 * absolute path to a user-picked/captured photo copied into app-private storage (see
 * `ImageStorageRepository`). Defaults to NULL for every existing row, which correctly means "no
 * custom photo — keep showing the icon/color" for every row that existed before this migration.
 *
 * Both columns are added in this single migration (rather than one migration per table) because
 * this app version has not been published yet — no installed build has ever run with only the
 * `category` column present, so there's no real-world database to preserve a separate step for.
 */
internal val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `category` ADD COLUMN `custom_image_path` TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE `account` ADD COLUMN `custom_image_path` TEXT DEFAULT NULL")
    }
}

/**
 * Adds the `transaction_attachment` table — one or more receipt/proof-of-payment photos per
 * transaction. Unlike Category/Account's single `custom_image_path` column, a transaction can
 * have any number of attachments, so this is a child table (same one-to-many shape as
 * `budget_category_relation`/`budget_account_relation`) rather than a column, with
 * `ON DELETE CASCADE` so deleting a transaction automatically drops its attachment rows. The
 * actual image files on disk are cleaned up separately by `ImageStorageRepository`, since Room
 * only owns the row, not the file.
 */
internal val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `transaction_attachment` (" +
                "`id` TEXT NOT NULL, " +
                "`transaction_id` TEXT NOT NULL, " +
                "`image_path` TEXT NOT NULL, " +
                "`created_on` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`transaction_id`) REFERENCES `transaction`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE)",
        )
    }
}

/**
 * Adds the `recurring_transaction` table (repeating transaction rules). New table only, so every
 * existing row is untouched. Must match RecurringTransactionEntity exactly, indices included,
 * or Room's schema validation fails on open.
 */
internal val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `recurring_transaction` (" +
                "`id` TEXT NOT NULL, " +
                "`notes` TEXT NOT NULL, " +
                "`category_id` TEXT NOT NULL, " +
                "`from_account_id` TEXT NOT NULL, " +
                "`to_account_id` TEXT, " +
                "`type` INTEGER NOT NULL, " +
                "`amount` REAL NOT NULL, " +
                "`frequency` TEXT NOT NULL, " +
                "`start_date` INTEGER NOT NULL, " +
                "`next_occurrence` INTEGER NOT NULL, " +
                "`next_due_date` INTEGER NOT NULL, " +
                "`created_on` INTEGER NOT NULL, " +
                "`updated_on` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`from_account_id`) REFERENCES `account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                "FOREIGN KEY(`to_account_id`) REFERENCES `account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                "FOREIGN KEY(`category_id`) REFERENCES `category`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transaction_from_account_id` ON `recurring_transaction` (`from_account_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transaction_to_account_id` ON `recurring_transaction` (`to_account_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transaction_category_id` ON `recurring_transaction` (`category_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transaction_next_due_date` ON `recurring_transaction` (`next_due_date`)")
    }
}

/**
 * Adds indices for the columns transaction queries filter, join and sort on. Indices only, so no
 * data changes. Names follow Room's `index_<table>_<column>` convention and must match the
 * @Index declarations on TransactionEntity and TransactionAttachmentEntity.
 */
internal val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_created_on` ON `transaction` (`created_on`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_from_account_id` ON `transaction` (`from_account_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_to_account_id` ON `transaction` (`to_account_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_category_id` ON `transaction` (`category_id`)")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_transaction_attachment_transaction_id` " +
                "ON `transaction_attachment` (`transaction_id`)",
        )
    }
}

/**
 * Adds an optional `name` to `budget`. NULL for every existing row, which shows the automatic
 * name ("This month's budget", "October 2026 budget", ...) exactly as before.
 */
internal val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `budget` ADD COLUMN `name` TEXT DEFAULT NULL")
    }
}

/**
 * Supplies the currency every pre-multi-currency (v11) row was implicitly in: the app's single
 * selected currency. Implemented outside this module (it lives in DataStore, not Room).
 */
fun interface LegacyCurrencyCodeProvider {
    /** A non-blank ISO 4217 code. */
    fun legacyCurrencyCode(): String
}

/**
 * Multi-currency support.
 *
 * Every v11 row was in the app's one selected currency, so that code is stamped on all accounts,
 * transactions (both sides) and budgets here, inside the migration: no row is ever left without
 * a currency. v11 transfers were same-currency by definition, so they need no rate snapshot.
 *
 * `account`, `transaction` and `budget` are altered in place, not rebuilt: dropping them would
 * fire their ON DELETE CASCADE children (attachments, budget links, recurring transactions). The
 * added NOT NULL columns carry a DEFAULT only because SQLite requires one for ALTER TABLE ADD
 * COLUMN; the entities declare none, and the use-case validation rejects blank codes.
 */
class Migration11To12(
    private val legacyCurrencyCodeProvider: LegacyCurrencyCodeProvider,
) : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val code = legacyCurrencyCodeProvider.legacyCurrencyCode()
        require(code.isNotBlank()) { "Legacy currency code must not be blank" }

        db.execSQL("ALTER TABLE `account` ADD COLUMN `currency_code` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `account` ADD COLUMN `is_archived` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE `account` SET `currency_code` = ?", arrayOf(code))

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `exchange_rate_snapshot` (" +
                "`id` TEXT NOT NULL, `source` INTEGER NOT NULL, `provider` TEXT, " +
                "`base_code` TEXT NOT NULL, `quote_code` TEXT NOT NULL, `rate` REAL NOT NULL, " +
                "`captured_at` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        )

        db.execSQL(
            "ALTER TABLE `transaction` ADD COLUMN `from_currency_code` TEXT NOT NULL DEFAULT ''",
        )
        db.execSQL(
            "ALTER TABLE `transaction` ADD COLUMN `to_currency_code` TEXT NOT NULL DEFAULT ''",
        )
        db.execSQL(
            "ALTER TABLE `transaction` ADD COLUMN `rate_snapshot_id` TEXT DEFAULT NULL " +
                "REFERENCES `exchange_rate_snapshot`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_transaction_rate_snapshot_id` " +
                "ON `transaction` (`rate_snapshot_id`)",
        )
        db.execSQL(
            "UPDATE `transaction` SET `from_currency_code` = ?, `to_currency_code` = ?",
            arrayOf(code, code),
        )

        db.execSQL("ALTER TABLE `budget` ADD COLUMN `currency_code` TEXT NOT NULL DEFAULT ''")
        db.execSQL("UPDATE `budget` SET `currency_code` = ?", arrayOf(code))

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `exchange_rates` (" +
                "`provider` TEXT NOT NULL, `base_code` TEXT NOT NULL, `target_code` TEXT NOT NULL, " +
                "`rate` REAL NOT NULL, `fetched_at` INTEGER NOT NULL, " +
                "PRIMARY KEY(`provider`, `base_code`, `target_code`))",
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `static_exchange_rate` (" +
                "`base_code` TEXT NOT NULL, `quote_code` TEXT NOT NULL, " +
                "`buy` REAL NOT NULL, `sell` REAL NOT NULL, `updated_on` INTEGER NOT NULL, " +
                "PRIMARY KEY(`base_code`, `quote_code`))",
        )
    }
}

/**
 * Every migration, oldest first. The database builder and the migration tests both use this list,
 * so a new migration can't be registered in one place and forgotten in the other.
 */
internal fun allMigrations(legacyCurrencyCodeProvider: LegacyCurrencyCodeProvider) = arrayOf(
    MIGRATION_1_2,
    MIGRATION_2_3,
    MIGRATION_3_4,
    MIGRATION_4_5,
    MIGRATION_5_6,
    MIGRATION_6_7,
    MIGRATION_7_8,
    MIGRATION_8_9,
    MIGRATION_9_10,
    MIGRATION_10_11,
    Migration11To12(legacyCurrencyCodeProvider),
)
