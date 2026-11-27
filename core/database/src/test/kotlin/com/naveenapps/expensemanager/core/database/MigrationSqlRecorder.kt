package com.naveenapps.expensemanager.core.database

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import java.lang.reflect.Proxy

/**
 * Records the SQL a migration runs, without a real database (OpenSpec change
 * move-database-to-sqlite-driver, task 1.1). Only the calls the migrations make are supported;
 * anything else fails, so a new kind of call can't slip by unrecorded.
 *
 * Migration 1→2 also reads: a count (answered 1, so the fallback-account path runs) and
 * `PRAGMA table_info` (answered with [CANNED_COLUMNS], as a v1 table that has them all). The
 * driver-based recorder that replaces this one gives the same answers, so both must record the
 * same SQL.
 */
class MigrationSqlRecorder {

    val statements = mutableListOf<String>()

    val database: SupportSQLiteDatabase = proxy(SupportSQLiteDatabase::class.java) { name, args ->
        when (name) {
            "execSQL" -> {
                val sql = args[0] as String
                // Migration 4→5 binds values; record them in the same form the SQLiteConnection
                // recorder uses for a prepared statement's bindings.
                statements += if (args.size > 1) sql + bindingsLine((args[1] as Array<*>).toList()) else sql
                Unit
            }

            "query" -> {
                val sql = args[0] as String
                statements += "QUERY: $sql"
                if (sql.startsWith("PRAGMA table_info")) columnsCursor() else countCursor(1)
            }

            else -> error("Migrations aren't expected to call SupportSQLiteDatabase.$name")
        }
    }

    private fun countCursor(count: Int): Cursor = proxy(Cursor::class.java) { name, args ->
        when (name) {
            "moveToFirst" -> true
            "getInt" -> count.also { require(args[0] == 0) }
            "getLong" -> count.toLong().also { require(args[0] == 0) }
            "close" -> Unit
            else -> error("Unexpected Cursor.$name on a count")
        }
    }

    private fun columnsCursor(): Cursor {
        var position = -1
        return proxy(Cursor::class.java) { name, args ->
            when (name) {
                "getColumnIndexOrThrow" -> NAME_COLUMN.also { require(args[0] == "name") }
                "moveToNext" -> (++position < CANNED_COLUMNS.size)
                "getString" -> CANNED_COLUMNS[position].also { require(args[0] == NAME_COLUMN) }
                "close" -> Unit
                else -> error("Unexpected Cursor.$name on PRAGMA table_info")
            }
        }
    }

    private fun <T> proxy(type: Class<T>, handler: (name: String, args: Array<Any?>) -> Any?): T = type.cast(
        Proxy.newProxyInstance(type.classLoader, arrayOf(type)) { _, method, args ->
            when (method.name) {
                "toString" -> "Recording ${type.simpleName}"
                "hashCode" -> System.identityHashCode(this)
                "equals" -> false
                else -> handler(method.name, args ?: emptyArray())
            }
        },
    )

    companion object {
        private const val NAME_COLUMN = 1

        /** How bound values are recorded after a statement's SQL (shared with the connection recorder). */
        fun bindingsLine(values: List<Any?>): String = "\nBIND: " + values.joinToString(", ") { "$it" }

        /** The old-table columns every `PRAGMA table_info` reports: current names and the older ones the migration maps. */
        val CANNED_COLUMNS = listOf(
            "id", "name", "type", "amount", "notes", "created_on", "updated_on",
            "background_color", "category_type", "account_id", "icon_name",
        )
    }
}
