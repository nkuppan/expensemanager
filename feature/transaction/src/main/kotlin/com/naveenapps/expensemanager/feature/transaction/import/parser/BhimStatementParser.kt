package com.naveenapps.expensemanager.feature.transaction.import.parser

import com.naveenapps.expensemanager.core.model.ParsedTransaction
import com.naveenapps.expensemanager.core.model.TransactionType
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale

class BhimStatementParser : StatementParser {

    override val displayName: String = "BHIM transaction history"

    override fun parse(text: String): List<ParsedTransaction> {
        if (text.isBlank()) return emptyList()
        return text.lines()
            .map { it.trimEnd() }
            .filter { it.isNotBlank() }
            .filterNot { isHeaderOrFooter(it) }
            .mapNotNull { parseLine(it) }
    }

    override fun canHandle(text: String): Boolean {
        return text.contains("Transaction History", ignoreCase = true) &&
            (text.contains("Payment ID", ignoreCase = true) ||
                text.contains("Reference Number", ignoreCase = true))
    }

    private fun isHeaderOrFooter(line: String): Boolean {
        val lower = line.lowercase()
        return lower.contains("transaction history") ||
            lower.contains("customer mobile number") ||
            (lower.contains("date") && lower.contains("bank name") && lower.contains("sender")) ||
            lower.startsWith("page no")
    }

    private fun parseLine(line: String): ParsedTransaction? {
        val dateMatch = DATE_TIME_REGEX.find(line) ?: return null
        val datePart = dateMatch.groupValues[1]
        val timePart = dateMatch.groupValues[2]

        // Tail columns are anchored to end-of-line so merchant/counterparty text
        // containing tokens like PAY, DR or decimal numbers cannot misparse.
        val tailMatch = TAIL_REGEX.find(line) ?: return null
        val referenceId = tailMatch.groupValues[1]
        val payCollect = tailMatch.groupValues[2].uppercase()
        val amount = tailMatch.groupValues[3].replace(",", "").toDoubleOrNull() ?: return null
        if (amount <= 0.0) return null
        val drCr = tailMatch.groupValues[4].uppercase()
        val status = tailMatch.groupValues[5].uppercase()
        val transactionType = if (drCr == "CR") TransactionType.INCOME else TransactionType.EXPENSE

        val dateTime = parseDateTime(datePart, timePart)

        val (bankName, accountNumber, senderRaw, receiverRaw) =
            extractMiddleColumns(line, dateMatch.range, tailMatch.range)
        if (bankName.isBlank() || accountNumber.isBlank()) return null

        val (senderVpa, senderName) = splitVpaAndName(senderRaw)
        val (receiverVpa, receiverName) = splitVpaAndName(receiverRaw)

        // Strict dates: never substitute today for a malformed date. The draft
        // stays invalid (parseError) and is blocked from import until corrected.
        var parseError: String? = null
        if (dateTime == null) {
            parseError = "Could not parse date - please correct"
        }

        return ParsedTransaction(
            amount = amount,
            transactionType = transactionType,
            dateTime = dateTime ?: Date(0),
            senderVpa = senderVpa,
            senderName = senderName,
            receiverVpa = receiverVpa,
            receiverName = receiverName,
            referenceId = referenceId,
            bankName = bankName,
            accountNumber = accountNumber,
            payOrCollect = payCollect,
            status = status,
            rawText = line,
            parseError = parseError,
        )
    }

    private fun parseDateTime(datePart: String, timePart: String): Date? {
        // DateTimeFormatter is immutable and thread-safe: parsing runs on
        // Dispatchers.Default and may overlap.
        return try {
            val local = LocalDateTime.parse("$datePart $timePart", DATE_TIME_FORMATTER)
            Date.from(local.atZone(ZoneId.systemDefault()).toInstant())
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private fun extractMiddleColumns(
        line: String,
        dateRange: IntRange,
        tailRange: IntRange,
    ): MiddleColumns {
        val withoutTail = line.substring(dateRange.last + 1, tailRange.first).trim()
        val parts = withoutTail.split(Regex("\\s{2,}")).map { it.trim() }.filter { it.isNotEmpty() }

        return when {
            parts.size >= 4 -> {
                MiddleColumns(
                    bankName = parts[0],
                    accountNumber = parts[1],
                    senderRaw = parts[2],
                    receiverRaw = parts.drop(3).joinToString("  "),
                )
            }
            parts.size == 3 -> {
                MiddleColumns(
                    bankName = parts[0],
                    accountNumber = "",
                    senderRaw = parts[1],
                    receiverRaw = parts[2],
                )
            }
            else -> {
                parseSingleSpaceMiddle(withoutTail)
            }
        }
    }

    private fun parseSingleSpaceMiddle(withoutTail: String): MiddleColumns {
        val accountMatch = ACCOUNT_REGEX.find(withoutTail)
        if (accountMatch == null) {
            return MiddleColumns("", "", "", withoutTail)
        }
        val bankName = withoutTail.substring(0, accountMatch.range.first).trim()
        val accountNumber = accountMatch.value
        val remainder = withoutTail.substring(accountMatch.range.last + 1).trim()
        val tokens = VPA_TOKEN_REGEX.findAll(remainder).map { it.value.trim() }.toList()
        return when {
            tokens.size >= 2 -> {
                MiddleColumns(
                    bankName = bankName,
                    accountNumber = accountNumber,
                    senderRaw = tokens[0],
                    receiverRaw = tokens.drop(1).joinToString(" "),
                )
            }
            tokens.size == 1 -> {
                MiddleColumns(
                    bankName = bankName,
                    accountNumber = accountNumber,
                    senderRaw = tokens[0],
                    receiverRaw = "",
                )
            }
            else -> {
                MiddleColumns(bankName, accountNumber, "", remainder)
            }
        }
    }

    private fun splitVpaAndName(raw: String): Pair<String, String> {
        val cleaned = raw.trim()
        if (cleaned.isBlank() || cleaned.equals("N/A", ignoreCase = true) || cleaned == "N/AN/A") {
            return "N/A" to ""
        }
        if (cleaned.contains("(") && cleaned.endsWith(")")) {
            val open = cleaned.lastIndexOf("(")
            val vpa = cleaned.substring(0, open).trim()
            val name = cleaned.substring(open + 1, cleaned.length - 1).trim()
            return (vpa.ifBlank { "N/A" }) to name
        }
        val normalized = cleaned.replace("N/AN/A", "N/A").trim()
        if (normalized.equals("N/A", ignoreCase = true)) return "N/A" to ""
        return normalized to ""
    }

    private data class MiddleColumns(
        val bankName: String,
        val accountNumber: String,
        val senderRaw: String,
        val receiverRaw: String,
    )

    companion object {
        private val DATE_TIME_REGEX = Regex("(\\d{2}/\\d{2}/\\d{4})\\s+(\\d{2}:\\d{2}:\\d{2})")
        private val TAIL_REGEX =
            Regex("(\\S+)\\s+(PAY|COLLECT)\\s+([\\d,]+\\.\\d{2})\\s+(DR|CR)\\s+(SUCCESS|FAILURE|PENDING)\\s*$")
        private val ACCOUNT_REGEX = Regex("\\b(?:XXXXXX\\d+|\\d+X+\\d+)\\b")
        private val VPA_TOKEN_REGEX = Regex("\\S+\\([^)]*\\)|\\S+")
        private val DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH)
    }
}
