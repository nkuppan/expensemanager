package com.naveenapps.expensemanager.feature.transaction.import.parser

import com.naveenapps.expensemanager.core.model.ParsedTransaction

fun interface StatementParser {
    fun parse(text: String): List<ParsedTransaction>

    /**
     * Whether this parser recognises the statement text. Used by
     * [StatementParserResolver] to pick a format automatically, so an
     * implementation must be strict enough not to claim another bank's layout.
     */
    fun canHandle(text: String): Boolean = false

    /** Format name, for the unsupported-statement error. */
    val displayName: String get() = "statement"
}
