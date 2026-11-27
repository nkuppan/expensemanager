package com.naveenapps.expensemanager.core.database

import androidx.sqlite.SQLITE_DATA_BLOB
import androidx.sqlite.SQLITE_DATA_FLOAT
import androidx.sqlite.SQLITE_DATA_INTEGER
import androidx.sqlite.SQLITE_DATA_NULL
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement

/*
 * Test helpers for the driver-based migration tests (OpenSpec change
 * move-database-to-sqlite-driver, design D6). The tests were written against SupportSQLite's
 * Cursor; these give a SQLiteConnection the same calls, so their assertions stay unchanged.
 */

/** Runs [sql] and returns all its rows, read up front, behind the Cursor calls the tests use. */
fun SQLiteConnection.query(sql: String, args: Array<out Any?> = emptyArray()): BufferedCursor = prepare(sql).use { statement ->
    statement.bindAll(args)
    val names = (0 until statement.getColumnCount()).map { statement.getColumnName(it) }
    val rows = buildList {
        while (statement.step()) {
            add(
                Array<Any?>(names.size) { column ->
                    when (statement.getColumnType(column)) {
                        SQLITE_DATA_NULL -> null
                        SQLITE_DATA_INTEGER -> statement.getLong(column)
                        SQLITE_DATA_FLOAT -> statement.getDouble(column)
                        SQLITE_DATA_BLOB -> statement.getBlob(column)
                        else -> statement.getText(column)
                    }
                },
            )
        }
    }
    BufferedCursor(names, rows)
}

/** [sql] with bound [args], as SupportSQLiteDatabase.execSQL(sql, bindArgs) did. */
fun SQLiteConnection.execSQL(sql: String, args: Array<out Any?>) {
    prepare(sql).use { statement ->
        statement.bindAll(args)
        statement.step()
    }
}

/** The database's schema version (SQLite `user_version`), as SupportSQLiteDatabase.version gave it. */
val SQLiteConnection.version: Int
    get() = query("PRAGMA user_version").use {
        it.moveToFirst()
        it.getInt(0)
    }

private fun SQLiteStatement.bindAll(args: Array<out Any?>) = args.forEachIndexed { i, value ->
    val index = i + 1
    when (value) {
        null -> bindNull(index)
        is String -> bindText(index, value)
        is Int -> bindLong(index, value.toLong())
        is Long -> bindLong(index, value)
        is Boolean -> bindLong(index, if (value) 1L else 0L)
        is Double -> bindDouble(index, value)
        is Float -> bindDouble(index, value.toDouble())
        is ByteArray -> bindBlob(index, value)
        else -> error("Can't bind ${value::class.simpleName}")
    }
}

/**
 * A read-only cursor over rows already read. Conversions follow android.database.Cursor: a NULL
 * reads as 0, and numbers read as text give their usual string form.
 */
class BufferedCursor(private val columnNames: List<String>, private val rows: List<Array<Any?>>) : AutoCloseable {

    private var position = -1

    val count: Int get() = rows.size

    fun moveToFirst(): Boolean {
        position = 0
        return rows.isNotEmpty()
    }

    fun moveToNext(): Boolean = ++position < rows.size

    fun getColumnIndexOrThrow(name: String): Int = columnNames.indexOf(name).also {
        require(it >= 0) { "No column '$name' in $columnNames" }
    }

    fun isNull(column: Int): Boolean = value(column) == null

    /**
     * The value as text. Cursor.getString's Java signature let the tests use it as a non-null
     * String; it's kept non-null here, and NULL fails clearly (check NULL with [isNull]).
     */
    fun getString(column: Int): String = when (val value = value(column)) {
        null -> error("Column $column is NULL; check it with isNull()")
        is ByteArray -> value.decodeToString()
        else -> value.toString()
    }

    fun getLong(column: Int): Long = when (val value = value(column)) {
        null -> 0L
        is Number -> value.toLong()
        else -> value.toString().toLongOrNull() ?: 0L
    }

    fun getInt(column: Int): Int = getLong(column).toInt()

    fun getDouble(column: Int): Double = when (val value = value(column)) {
        null -> 0.0
        is Number -> value.toDouble()
        else -> value.toString().toDoubleOrNull() ?: 0.0
    }

    override fun close() = Unit

    private fun value(column: Int): Any? {
        check(position in rows.indices) { "Cursor isn't on a row (position $position of ${rows.size})" }
        return rows[position][column]
    }
}
