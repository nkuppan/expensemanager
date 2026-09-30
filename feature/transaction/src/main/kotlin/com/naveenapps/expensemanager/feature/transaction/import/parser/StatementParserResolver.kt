package com.naveenapps.expensemanager.feature.transaction.import.parser

/**
 * Picks the parser for an extracted statement.
 *
 * Formats are tried in order, so a parser that recognises a format broadly must
 * come after one that recognises it precisely. The built-in parsers key off
 * mutually exclusive markers: the BHIM transaction history carries a
 * "Transaction History" heading with payment-id columns, the SBI YONO account
 * statement a "Statement of Account" heading with transfer markers.
 */
class StatementParserResolver(
    private val parsers: List<StatementParser> = listOf(
        BhimStatementParser(),
        SbiYonoStatementParser(),
    ),
) {
    fun select(text: String): StatementParser? = parsers.firstOrNull { it.canHandle(text) }

    /** Format names, for the unsupported-statement error. */
    val supportedFormats: List<String> get() = parsers.map { it.displayName }
}
