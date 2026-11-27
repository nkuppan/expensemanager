package com.naveenapps.expensemanager.core.database

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import java.lang.reflect.Proxy

/**
 * Records the SQL a migration runs on a [SQLiteConnection], without a real database (OpenSpec
 * change move-database-to-sqlite-driver, design D2). Only the calls the migrations make are
 * supported; anything else fails, so a new kind of call can't slip by unrecorded.
 *
 * The output format and the canned answers are the ones the golden file was recorded with from
 * the SupportSQLite migrations (task 1.2):
 * - a statement run through `execSQL` or `prepare`+`step` is recorded as its SQL, followed by a
 *   [bindingsLine] when values were bound (migration 4→5);
 * - a read (`SELECT …`, `PRAGMA table_info`) is recorded as `QUERY: <sql>` when prepared. The
 *   count is answered 1 (so the fallback-account path runs) and `PRAGMA table_info` lists
 *   [CANNED_COLUMNS], as a v1 table that has them all.
 */
class MigrationSqlRecorder {

    val statements = mutableListOf<String>()

    val connection: SQLiteConnection = proxy(SQLiteConnection::class.java) { name, args ->
        when (name) {
            "prepare" -> statement(args[0] as String)
            "inTransaction" -> true
            "close" -> Unit
            else -> error("Migrations aren't expected to call SQLiteConnection.$name")
        }
    }

    private fun statement(sql: String): SQLiteStatement = when {
        sql.startsWith("PRAGMA table_info") -> columnsStatement(sql)
        sql.startsWith("SELECT") -> countStatement(sql)
        else -> writeStatement(sql)
    }

    private fun writeStatement(sql: String): SQLiteStatement {
        val bindings = sortedMapOf<Int, Any?>()
        var stepped = false
        return proxy(SQLiteStatement::class.java) { name, args ->
            when (name) {
                "bindText", "bindLong", "bindDouble", "bindBlob" -> bindings[args[0] as Int] = args[1]

                "bindNull" -> bindings[args[0] as Int] = null

                "step" -> {
                    check(!stepped) { "A write statement is stepped once" }
                    stepped = true
                    statements += if (bindings.isEmpty()) sql else sql + bindingsLine(bindings.values.toList())
                    false
                }

                "close" -> Unit

                else -> error("Unexpected SQLiteStatement.$name on a write")
            }
        }
    }

    private fun countStatement(sql: String): SQLiteStatement {
        statements += "QUERY: $sql"
        var stepped = false
        return proxy(SQLiteStatement::class.java) { name, args ->
            when (name) {
                "step" -> (!stepped).also { stepped = true }
                "getLong" -> 1L.also { require(args[0] == 0) }
                "getInt" -> 1.also { require(args[0] == 0) }
                "close" -> Unit
                else -> error("Unexpected SQLiteStatement.$name on a count")
            }
        }
    }

    private fun columnsStatement(sql: String): SQLiteStatement {
        statements += "QUERY: $sql"
        var position = -1
        return proxy(SQLiteStatement::class.java) { name, args ->
            when (name) {
                "getColumnCount" -> TABLE_INFO_COLUMNS.size
                "getColumnName" -> TABLE_INFO_COLUMNS[args[0] as Int]
                "getColumnNames" -> TABLE_INFO_COLUMNS
                "step" -> (++position < CANNED_COLUMNS.size)
                "getText" -> CANNED_COLUMNS[position].also { require(TABLE_INFO_COLUMNS[args[0] as Int] == "name") }
                "close" -> Unit
                else -> error("Unexpected SQLiteStatement.$name on PRAGMA table_info")
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
        /** The result columns of `PRAGMA table_info`, as SQLite reports them. */
        private val TABLE_INFO_COLUMNS = listOf("cid", "name", "type", "notnull", "dflt_value", "pk")

        /** How bound values are recorded after a statement's SQL (the golden file's format). */
        fun bindingsLine(values: List<Any?>): String = "\nBIND: " + values.joinToString(", ") { "$it" }

        /** The old-table columns every `PRAGMA table_info` reports: current names and the older ones the migration maps. */
        val CANNED_COLUMNS = listOf(
            "id", "name", "type", "amount", "notes", "created_on", "updated_on",
            "background_color", "category_type", "account_id", "icon_name",
        )
    }
}
