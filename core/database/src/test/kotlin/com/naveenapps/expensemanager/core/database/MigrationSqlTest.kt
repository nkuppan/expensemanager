package com.naveenapps.expensemanager.core.database

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The SQL every migration runs, pinned in `src/test/resources/migration-sql.golden.txt` (OpenSpec
 * change move-database-to-sqlite-driver, design D2). The golden file was recorded from the
 * SupportSQLite migrations before they moved to SQLiteConnection, so the rewrite can't change a
 * statement unnoticed.
 *
 * To re-record after a deliberate change: `./gradlew :core:database:testDebugUnitTest -PrecordMigrationSql=true`.
 */
class MigrationSqlTest {

    private val golden = File(requireNotNull(System.getProperty("migrationSqlGolden")) { "Run through Gradle" })
    private val record = System.getProperty("recordMigrationSql") == "true"

    private fun recordAll(): String = ALL_MIGRATIONS.joinToString("") { migration ->
        val recorder = MigrationSqlRecorder()
        migration.migrate(recorder.connection)
        "=== ${migration.startVersion} -> ${migration.endVersion}\n" + recorder.statements.joinToString("") { "$it\n;;\n" }
    }

    @Test
    fun migrationsRunExactlyThePinnedSql() {
        val actual = recordAll()
        if (record) {
            golden.parentFile.mkdirs()
            golden.writeText(actual)
            println("Recorded ${ALL_MIGRATIONS.size} migrations to $golden")
            return
        }
        assertTrue("No golden file at $golden: record it with -PrecordMigrationSql=true", golden.exists())
        assertEquals(golden.readText(), actual)
    }

    @Test
    fun everyMigrationIsPinned() {
        assertEquals((1 until DATABASE_VERSION).map { it to it + 1 }, ALL_MIGRATIONS.map { it.startVersion to it.endVersion })
    }
}
