package com.naveenapps.expensemanager.feature.transaction.import.parser

import com.naveenapps.expensemanager.core.model.ParsedTransaction
import com.naveenapps.expensemanager.core.model.TransactionType
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Date
import java.util.Locale

/**
 * SBI YONO "Statement of Account" PDFs.
 *
 * A transaction starts a line with a transaction date and a posting date,
 * followed by a description and the trailing money columns:
 *
 * ```
 * 01/06/2025  01/06/2025  UPI/DR/800000000001/ALPHA MART  -  111.11  -  12,34,567.89
 *                           /TESTBNK/MOCKSHOP/MOCK REM
 *                           0000000000 AT 00000 TESTBR
 * ```
 *
 * Four properties of the format drive the implementation:
 *
 *  - Descriptions wrap onto indented continuation lines, so a row is only
 *    complete once the next date-prefixed line (or the end of the text) is
 *    reached.
 *  - A transfer marker such as `WDL TFR` is printed on its own line directly
 *    above the row it describes, which in the flattened text means it trails
 *    the *previous* row. The marker is therefore read as a pending hint for the
 *    next row instead of being appended to the current description.
 *  - The money columns are the trailing tokens of the row line. One of
 *    withdrawal/deposit carries an amount, the other is `-`, and the last
 *    column is the running balance. Parsing from the end of the line keeps
 *    description text containing digits or dashes intact.
 *  - Rows carry no time of day, so drafts are flagged date-only and duplicate
 *    detection falls back to same-day matching.
 *
 * Amounts use Indian digit grouping (`1,00,003.00`).
 */
class SbiYonoStatementParser : StatementParser {

    override val displayName: String = "SBI YONO account statement"

    override fun canHandle(text: String): Boolean {
        val hasStatementHeader = text.contains(STATEMENT_HEADER, ignoreCase = true)
        val hasTransactionRows = UPI_ROW_REGEX.containsMatchIn(text) ||
            text.contains("WDL TFR", ignoreCase = true) ||
            text.contains("DEP TFR", ignoreCase = true)
        return hasStatementHeader && hasTransactionRows
    }

    override fun parse(text: String): List<ParsedTransaction> {
        if (text.isBlank()) return emptyList()

        val accountNumber = findHolderAccountNumber(text)
        val transactions = mutableListOf<ParsedTransaction>()
        var pending: PendingRow? = null
        var pendingMarker: String? = null

        for (line in text.lines()) {
            val trimmed = line.trim()
            if (TRANSFER_MARKER_REGEX.matches(trimmed)) {
                pendingMarker = trimmed
                continue
            }
            val row = matchRow(line)
            if (row != null) {
                pending?.let { transactions += it.toParsedTransaction(accountNumber) }
                pending = row.copy(marker = pendingMarker)
                pendingMarker = null
            } else if (pending != null && isDescriptionContinuation(trimmed)) {
                pending = pending.withContinuation(trimmed)
            }
        }
        pending?.let { transactions += it.toParsedTransaction(accountNumber) }

        return transactions
    }

    /**
     * Matches a transaction row. A line only starts a row when it also carries
     * a well-formed money tail, so a wrapped description that happens to begin
     * with a date cannot truncate the previous row.
     */
    private fun matchRow(line: String): PendingRow? {
        val match = ROW_REGEX.matchEntire(line.trimEnd()) ?: return null
        val tail = parseTail(match.groupValues[2]) ?: return null
        return PendingRow(
            datePart = match.groupValues[1],
            descriptionHead = tail.description,
            withdrawal = tail.withdrawal,
            deposit = tail.deposit,
        )
    }

    /**
     * The money columns are the trailing tokens of the row line. The instrument
     * column is only recognised as the statement's `-` placeholder, so a
     * statement without that column can never be read one token too far left.
     */
    private fun parseTail(remainder: String): MoneyTail? {
        val tokens = remainder.trim().split(WHITESPACE_REGEX)
        for (shape in TAIL_SHAPES) {
            if (tokens.size < shape.size) continue
            val tail = tokens.takeLast(shape.size)
            if (shape.instrumentPlaceholder && tail[0] != DASH) continue
            val withdrawal = parseAmountOrDash(tail[shape.withdrawalIndex])
            val deposit = parseAmountOrDash(tail[shape.depositIndex])
            // Exactly one of the two columns carries the amount: two dashes or
            // two amounts means this token split is not the money tail.
            if ((withdrawal != null) == (deposit != null)) continue
            if (shape.balanceIndex >= 0 && parseAmount(tail[shape.balanceIndex]) == null) continue
            return MoneyTail(
                withdrawal = withdrawal,
                deposit = deposit,
                description = tokens.dropLast(shape.size).joinToString(" "),
            )
        }
        return null
    }

    private fun parseAmountOrDash(token: String): Double? {
        return if (token == DASH) null else parseAmount(token)
    }

    private fun parseAmount(token: String): Double? {
        if (token == DASH) return null
        return token.replace(",", "").toDoubleOrNull()
    }

    private fun isDescriptionContinuation(trimmedLine: String): Boolean {
        // The page footer is a row of empty brackets and carries no text.
        if (!trimmedLine.any { it.isLetterOrDigit() }) return false
        return !isNoise(trimmedLine)
    }

    private fun isNoise(line: String): Boolean {
        return line.equals(STATEMENT_HEADER, ignoreCase = true) ||
            line.equals(BANK_NAME, ignoreCase = true) ||
            line.equals("Balance", ignoreCase = true) ||
            line.startsWith("Page no", ignoreCase = true) ||
            SUMMARY_LINE_REGEX.matches(line.lowercase())
    }

    /**
     * The holder's account number only appears in the statement header, which
     * PDF text extraction places before the first row. Returns `N/A` when the
     * statement does not expose it.
     */
    private fun findHolderAccountNumber(text: String): String {
        val header = text.lineSequence().take(HEADER_SCAN_LINES).joinToString(" ")
        val labelled = LABELLED_ACCOUNT_REGEX.find(header)?.groupValues?.get(1)
        if (!labelled.isNullOrBlank()) return labelled
        return MASKED_ACCOUNT_REGEX.find(header)?.value ?: NO_VALUE
    }

    private fun parseDate(datePart: String): Date? {
        // DateTimeFormatter is immutable and thread-safe: parsing runs on
        // Dispatchers.Default and may overlap. STRICT rejects an impossible date
        // such as 31/02 instead of silently shifting it into the next month.
        return try {
            val local = LocalDate.parse(datePart, DATE_FORMATTER)
            Date.from(local.atStartOfDay(ZoneId.systemDefault()).toInstant())
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private data class MoneyTail(
        val withdrawal: Double?,
        val deposit: Double?,
        val description: String,
    )

    /**
     * A money-column shape, described by where withdrawal, deposit and the
     * running balance sit inside the trailing tokens of a row line. The balance
     * is optional, and the instrument column is only claimed when the statement
     * prints the `-` placeholder there.
     */
    private data class TailShape(
        val size: Int,
        val withdrawalIndex: Int,
        val depositIndex: Int,
        val balanceIndex: Int,
        val instrumentPlaceholder: Boolean = false,
    )

    private data class Counterparty(
        val referenceId: String,
        val name: String,
    )

    private data class PendingRow(
        val datePart: String,
        val descriptionHead: String,
        val withdrawal: Double?,
        val deposit: Double?,
        val marker: String? = null,
        val continuations: List<String> = emptyList(),
    ) {
        fun withContinuation(line: String) = copy(continuations = continuations + line)
    }

    private fun PendingRow.toParsedTransaction(accountNumber: String): ParsedTransaction {
        // The bank's own transfer marker wins over the column layout: a
        // description ending in "-" would otherwise shift the money columns and
        // invert the direction.
        val isWithdrawal = when {
            marker == null -> withdrawal != null
            else -> WITHDRAWAL_MARKERS.any { marker.startsWith(it, ignoreCase = true) }
        }
        val amount = if (isWithdrawal) {
            withdrawal ?: deposit ?: 0.0
        } else {
            deposit ?: withdrawal ?: 0.0
        }
        val isIncome = !isWithdrawal
        val fragments = listOf(descriptionHead) + continuations
        val counterparty = extractCounterparty(fragments)
        val dateTime = parseDate(datePart)

        // Strict dates: never substitute today for a malformed date. The draft
        // stays invalid (parseError) and is blocked from import until corrected,
        // matching the BHIM parser's behaviour.
        val parseError = if (dateTime == null) "Could not parse date - please correct" else null

        return ParsedTransaction(
            amount = amount,
            transactionType = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
            dateTime = dateTime ?: Date(0),
            senderVpa = NO_VALUE,
            senderName = if (isIncome) counterparty.name else "",
            receiverVpa = NO_VALUE,
            receiverName = if (isIncome) "" else counterparty.name,
            referenceId = counterparty.referenceId,
            bankName = BANK_NAME,
            accountNumber = accountNumber,
            payOrCollect = if (isIncome) COLLECT else PAY,
            status = STATUS_SUCCESS,
            rawText = fragments.joinToString(" "),
            parseError = parseError,
            isDateOnly = true,
        )
    }

    /**
     * Splits the statement description into a reference number and a
     * counterparty name.
     *
     * UPI rows read `UPI/<DR|CR>/<ref>/<name…>` on the row line, so both values
     * come from there. NEFT/IMPS/RTGS rows put a `*`-separated URN on the row
     * line and the beneficiary on the wrapped line. Anything else (interest,
     * dividends, cash transactions) has no reference and uses the description
     * itself as the name.
     *
     * The description column is clipped at the column width, so a name can be
     * truncated mid-word and is taken as printed.
     */
    private fun extractCounterparty(fragments: List<String>): Counterparty {
        val head = fragments.firstOrNull().orEmpty()

        UPI_HEAD_REGEX.find(head)?.let { match ->
            return Counterparty(
                referenceId = match.groupValues[1],
                name = match.groupValues[2].substringBefore('/').cleanName(),
            )
        }

        TRANSFER_HEAD_REGEX.find(head)?.let { match ->
            val urnParts = match.groupValues[1].split(URN_SEPARATOR).filter { it.isNotBlank() }
            val codeCount = urnParts.takeWhile { URN_CODE_REGEX.matches(it) }.size
            val inLineName = urnParts.drop(codeCount).joinToString(" ")
            val wrappedName = fragments.drop(1)
                .firstOrNull { !ACCOUNT_LINE_REGEX.matches(it.trim()) }
                .orEmpty()
            return Counterparty(
                referenceId = urnParts.take(codeCount).joinToString("*"),
                name = (if (inLineName.isNotBlank()) inLineName else wrappedName)
                    .replace(LEADING_URN_REGEX, "")
                    .replace(TRAILING_MARKER_REGEX, "")
                    .cleanName(),
            )
        }

        val plain = fragments
            .filterNot { ACCOUNT_LINE_REGEX.matches(it.trim()) }
            .joinToString(" ")
        return Counterparty(referenceId = "", name = plain.cleanName())
    }

    private fun String.cleanName(): String = trim().replace(WHITESPACE_REGEX, " ")

    companion object {
        const val BANK_NAME = "State Bank of India"

        private const val STATEMENT_HEADER = "STATEMENT OF ACCOUNT"
        private const val NO_VALUE = "N/A"
        private const val DASH = "-"
        private const val PAY = "PAY"
        private const val COLLECT = "COLLECT"
        private const val STATUS_SUCCESS = "SUCCESS"
        private const val HEADER_SCAN_LINES = 40

        /** instrument, withdrawal, deposit, balance - then the same without the
         *  instrument column, and finally a statement with no balance column. */
        private val TAIL_SHAPES = listOf(
            TailShape(size = 4, withdrawalIndex = 1, depositIndex = 2, balanceIndex = 3, instrumentPlaceholder = true),
            TailShape(size = 3, withdrawalIndex = 0, depositIndex = 1, balanceIndex = 2),
            TailShape(size = 2, withdrawalIndex = 0, depositIndex = 1, balanceIndex = -1),
        )
        private val WITHDRAWAL_MARKERS = listOf("WDL", "DR")

        private val WHITESPACE_REGEX = Regex("\\s+")
        private val ROW_REGEX =
            Regex("^\\s*(\\d{1,2}/\\d{1,2}/\\d{2,4})\\s+\\d{1,2}/\\d{1,2}/\\d{2,4}\\s+(.*)$")
        private val UPI_ROW_REGEX = Regex("UPI/(?:DR|CR)/\\d{4,}/", RegexOption.IGNORE_CASE)
        private val UPI_HEAD_REGEX =
            Regex("UPI/(?:DR|CR)/(\\d{4,})/([^/]*)", RegexOption.IGNORE_CASE)
        private val TRANSFER_HEAD_REGEX =
            Regex("^(?:NEFT|IMPS|RTGS|NECS)\\*(.*)$", RegexOption.IGNORE_CASE)
        private val TRANSFER_MARKER_REGEX = Regex("^[A-Z]{2,4} TFR$")
        private val SUMMARY_LINE_REGEX = Regex(
            "^(opening balance|closing balance|available balance|account holder|a/?c|account|" +
                "branch|ifsc|micr|start date|end date|generated on|total)\\b.*",
        )
        private val LABELLED_ACCOUNT_REGEX = Regex(
            "(?:a/?c|account)\\s*(?:no\\.?|number)?\\s*[:.]?\\s*([Xx\\d]{6,})",
            RegexOption.IGNORE_CASE,
        )
        private val MASKED_ACCOUNT_REGEX = Regex("\\bX{3,}\\d{3,}\\b")
        private val ACCOUNT_LINE_REGEX = Regex("^\\d{6,}\\s+AT\\s+\\d{5}\\b")
        private val URN_SEPARATOR = Regex("\\*")
        private val URN_CODE_REGEX = Regex("[A-Z0-9]{4,}")
        private val LEADING_URN_REGEX = Regex("^\\d+\\s*\\*\\s*")
        private val TRAILING_MARKER_REGEX = Regex("\\s*\\*\\s*[A-Z]\\s*$")
        private val DATE_FORMATTER = DateTimeFormatter
            .ofPattern("d/M/uuuu", Locale.ENGLISH)
            .withResolverStyle(ResolverStyle.STRICT)
    }
}
