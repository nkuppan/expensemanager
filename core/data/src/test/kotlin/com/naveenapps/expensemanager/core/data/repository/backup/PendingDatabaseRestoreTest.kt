package com.naveenapps.expensemanager.core.data.repository.backup

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.database.PendingDatabaseRestore
import java.io.File
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PendingDatabaseRestoreTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val name = "restore_test.db"

    private fun databaseBytes(vararg tables: String): ByteArray {
        val file = File(context.cacheDir, "fixture-${tables.joinToString("-")}.db").apply { delete() }
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            tables.forEach { db.execSQL("CREATE TABLE `$it` (id TEXT PRIMARY KEY)") }
        }
        return file.readBytes().also { file.delete() }
    }

    @Test
    fun appliedRestoreCanBeRolledBackToThePreviousDatabase() {
        val databaseFile = context.getDatabasePath(name).apply { parentFile?.mkdirs() }
        databaseFile.writeBytes(byteArrayOf(1, 2, 3))
        File(databaseFile.path + "-wal").writeBytes(byteArrayOf(9))

        PendingDatabaseRestore.stage(context, name, byteArrayOf(4, 5, 6))
        assertThat(PendingDatabaseRestore.applyIfPending(context, name)).isTrue()
        assertThat(databaseFile.readBytes()).isEqualTo(byteArrayOf(4, 5, 6))
        assertThat(File(databaseFile.path + "-wal").exists()).isFalse()

        assertThat(PendingDatabaseRestore.rollback(context, name)).isTrue()
        assertThat(databaseFile.readBytes()).isEqualTo(byteArrayOf(1, 2, 3))
        // The un-checkpointed WAL of the original database comes back with it.
        assertThat(File(databaseFile.path + "-wal").readBytes()).isEqualTo(byteArrayOf(9))
    }

    @Test
    fun confirmedRestoreCannotBeRolledBack() {
        val databaseFile = context.getDatabasePath(name).apply { parentFile?.mkdirs() }
        databaseFile.writeBytes(byteArrayOf(1))

        PendingDatabaseRestore.stage(context, name, byteArrayOf(2))
        PendingDatabaseRestore.applyIfPending(context, name)
        PendingDatabaseRestore.confirmRestored(context)

        assertThat(PendingDatabaseRestore.rollback(context, name)).isFalse()
        assertThat(databaseFile.readBytes()).isEqualTo(byteArrayOf(2))
    }

    @Test
    fun nothingStagedMeansNothingApplied() {
        assertThat(PendingDatabaseRestore.applyIfPending(context, "never_staged.db")).isFalse()
    }

    @Test
    fun recognisesExpenseManagerDatabases() {
        val ours = databaseBytes("account", "category", "transaction", "budget")
        assertThat(PendingDatabaseRestore.missingRequiredTables(context, ours)).isEmpty()
    }

    @Test
    fun rejectsOtherSqliteFilesAndGarbage() {
        val other = databaseBytes("notes")
        assertThat(PendingDatabaseRestore.missingRequiredTables(context, other))
            .containsExactly("account", "category", "transaction")
        assertThat(PendingDatabaseRestore.missingRequiredTables(context, ByteArray(512) { 7 }))
            .isNotEmpty()
    }
}
