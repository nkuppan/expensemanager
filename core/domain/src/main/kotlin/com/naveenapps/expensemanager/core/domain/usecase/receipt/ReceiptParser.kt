package com.naveenapps.expensemanager.core.domain.usecase.receipt

import com.naveenapps.expensemanager.core.model.TransactionType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale

/** What could be read from a shared payment receipt; any part may be missing. */
data class ParsedReceipt(
    val amount: Double? = null,
    /** Who was paid (or who paid, for income): becomes the transaction's notes. */
    val payee: String? = null,
    val date: Date? = null,
    val type: TransactionType = TransactionType.EXPENSE,
    /** Last digits of the card or account it was paid from ("XXXX1234" → "1234"). */
    val accountDigits: List<String> = emptyList(),
)

/**
 * Reads a payment receipt: the text of a UPI / wallet / bank message shared into the app, or the
 * text recognised on the phone from a screenshot (GPay, PhonePe) or a photo of a shop receipt.
 * Everything runs on the device. It's deliberately forgiving: the user always sees and confirms
 * the result.
 */
object ReceiptParser {

    fun parse(text: String, now: Date = Date(), zone: ZoneId = ZoneId.systemDefault()): ParsedReceipt {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val lower = text.lowercase(Locale.ROOT)
        val type = if (INCOME_WORDS.any { it in lower } && OUTGOING_WORDS.none { it in lower }) {
            TransactionType.INCOME
        } else {
            TransactionType.EXPENSE
        }
        val amount = findBillTotal(lines) ?: findAmount(lines)
        return ParsedReceipt(
            amount = amount,
            // A shop bill rarely says "paid to": the shop's name is at the top.
            payee = findPayee(lines, type) ?: amount?.let { findMerchant(lines) },
            date = findDate(text, now, zone),
            type = type,
            accountDigits = findAccountDigits(text),
        )
    }

    // region amount

    // A money number in any common style: "1,00,000" (India), "1,234.56", "1.234,56" and "19,96"
    // (Europe), "250", "15000.50". Which separator is the decimal one is decided in toAmount.
    private const val NUMBER =
        """(?<![0-9])(?<![0-9][.,])(\d{1,2}(?:,\d{2})*,\d{3}(?:\.\d{1,2})?|\d{1,3}(?:[.,]\d{3})*(?:[.,]\d{1,2})?|""" +
            """\d{1,7}(?:[.,]\d{1,2})?)(?![0-9])"""

    private const val CURRENCY = """(?:₹|€|£|[$]|\brs\.?|\binr\b|\beur\b|\busd\b|\bgbp\b)"""

    // "₹250", "Rs. 1,240", "€ 19,96", and the symbol after the number too: "19,96 €", "12.50 EUR".
    private val CURRENCY_AMOUNT = Regex(
        """$CURRENCY\s*$NUMBER|$NUMBER\s*(?:€|₹|\beur\b|\binr\b)""",
        RegexOption.IGNORE_CASE,
    )

    // A line that is only a number, like the big "250" on a payment screen.
    private val BARE_AMOUNT = Regex("""^[₹€£$*]?\s*$NUMBER$""")

    private val AMOUNT_HINTS = listOf(
        "paid",
        "sent",
        "debited",
        "amount",
        "received",
        "credited",
        "payment of",
        "total",
    )
    private val NOT_THE_AMOUNT = listOf("balance", "bal", "limit", "cashback", "reward")

    internal fun findAmount(lines: List<String>): Double? {
        // Every currency amount, except balances, limits and rewards ("Avl Bal Rs 12,345").
        val found = lines.flatMap { line ->
            CURRENCY_AMOUNT.findAll(line).filterNot { it.isNotTheAmount(line) }.map { line to it }.toList()
        }
        // 1. One on a line that says what it is ("Paid ₹250", "Rs 5,000 credited").
        found.firstOrNull { (line, _) -> AMOUNT_HINTS.any { it in line.lowercase(Locale.ROOT) } }
            ?.second?.toAmount()?.let { return it }
        // 2. The first one anywhere.
        found.firstNotNullOfOrNull { (_, match) -> match.toAmount() }?.let { return it }
        // 3. A line that is just a number (screenshots where ₹ wasn't recognised).
        return lines
            .filterNot { line -> NOT_THE_AMOUNT.any { it in line.lowercase(Locale.ROOT) } }
            .firstNotNullOfOrNull { line -> BARE_AMOUNT.matchEntire(line)?.toAmount() }
    }

    private fun MatchResult.isNotTheAmount(line: String): Boolean {
        val before = line.substring(maxOf(0, range.first - LOOK_BEHIND), range.first).lowercase(Locale.ROOT)
        return NOT_THE_AMOUNT.any { it in before }
    }

    private const val LOOK_BEHIND = 15

    // The number is whichever group matched (CURRENCY_AMOUNT has one before and one after).
    private fun MatchResult.toAmount(): Double? =
        groupValues.drop(1).firstOrNull { it.isNotEmpty() }?.let(::parseNumber)?.takeIf { it > 0.0 }

    /**
     * Reads "1,00,000", "1,234.56", "1.234,56" or "19,96". The last separator is the decimal one
     * when one or two digits follow it (or the number starts "0,"); every other separator groups
     * thousands.
     */
    internal fun parseNumber(raw: String): Double? {
        val lastSeparator = raw.indexOfLast { it == ',' || it == '.' }
        if (lastSeparator < 0) return raw.toDoubleOrNull()
        val decimals = raw.length - lastSeparator - 1
        val isDecimal = decimals in 1..2 || raw.startsWith("0") && lastSeparator == 1
        return if (isDecimal) {
            (raw.substring(0, lastSeparator).filter { it.isDigit() } + "." + raw.substring(lastSeparator + 1))
                .toDoubleOrNull()
        } else {
            raw.filter { it.isDigit() }.toDoubleOrNull()
        }
    }

    // endregion

    // A printed bill's total line: "TOTAL 450.00", "Grand Total : 1,240", "Net Payable 99".
    // Sub-totals, tax and item counts are skipped; the largest total is the one paid.
    // Also the usual words on European and Indonesian bills ("Summe", "Montant", "Importe").
    private val TOTAL_LINE = Regex(
        """\b(?:grand\s+total|net\s+total|net\s+amount|net\s+payable|amount\s+payable|bill\s+amount|""" +
            """total\s+amount|total\s+due|total|summe|gesamtbetrag|gesamt|zu\s+zahlen|montant|totale|""" +
            """importe|a\s+pagar|jumlah|toplam|razem|suma)\b[^0-9]*?$NUMBER""",
        RegexOption.IGNORE_CASE,
    )
    private val NOT_A_TOTAL = listOf("sub total", "subtotal", "sub-total", "qty", "items", "tax", "saving", "discount")

    internal fun findBillTotal(lines: List<String>): Double? = lines
        .filterNot { line -> NOT_A_TOTAL.any { it in line.lowercase(Locale.ROOT) } }
        .mapNotNull { line -> TOTAL_LINE.find(line)?.toAmount() }
        .maxOrNull()

    // endregion

    // region payee

    private val INCOME_WORDS = listOf("received from", "credited", "money received", "you received")
    private val OUTGOING_WORDS = listOf("paid to", "sent to", "debited", "you paid", "payment to")

    private val EXPENSE_PAYEE = Regex(
        """^(?:paid to|sent to|payment to|transferred to|to)\b\s*[:\-]?\s*(.*)$""",
        RegexOption.IGNORE_CASE,
    )
    private val INCOME_PAYEE = Regex(
        """^(?:received from|from|credited by)\b\s*[:\-]?\s*(.*)$""",
        RegexOption.IGNORE_CASE,
    )

    // Within a sentence: "Paid Rs.120 to Chai Point on 07-11-2026", "... by UPI from ARUN K."
    private val EXPENSE_PAYEE_INLINE = Regex(
        """\b(?:paid|sent|transferred)\b.*?\bto\s+([a-z][^.\n]*?)(?:\s+on\b|\s+via\b|\s+using\b|\.|$)""",
        RegexOption.IGNORE_CASE,
    )
    private val INCOME_PAYEE_INLINE = Regex(
        """\b(?:received|credited)\b.*?\bfrom\s+([a-z][^.\n]*?)(?:\s+on\b|\s+via\b|\.|$)""",
        RegexOption.IGNORE_CASE,
    )

    internal fun findPayee(lines: List<String>, type: TransactionType): String? {
        val pattern = if (type == TransactionType.INCOME) INCOME_PAYEE else EXPENSE_PAYEE
        lines.forEachIndexed { index, line ->
            val match = pattern.matchEntire(line) ?: return@forEachIndexed
            // "Paid to" on its own line: the name is on the next one.
            val name = match.groupValues[1].ifBlank { lines.getOrNull(index + 1).orEmpty() }
            cleanName(name)?.let { return it }
        }
        val inline = if (type == TransactionType.INCOME) INCOME_PAYEE_INLINE else EXPENSE_PAYEE_INLINE
        return lines.firstNotNullOfOrNull { line -> inline.find(line)?.groupValues?.get(1)?.let(::cleanName) }
    }

    private fun cleanName(raw: String): String? {
        var name = raw.trim().trimEnd('.', ',', ':')
        // A UPI ID alone ("swiggy@icici"): the handle is the readable part.
        if ('@' in name && ' ' !in name) name = name.substringBefore('@')
        // Not a name: an amount, an ID or a date.
        val mostlyDigits = name.count { it.isDigit() } > name.length / 2
        if (name.isBlank() || CURRENCY_AMOUNT.containsMatchIn(name) || mostlyDigits) return null
        return name.take(MAX_PAYEE_LENGTH).trim()
    }

    private const val MAX_PAYEE_LENGTH = 40

    // Headings and details printed near a shop's name (whole words, so "Hotel" isn't "tel").
    private val NOT_A_MERCHANT = Regex(
        """\b(?:tax invoice|invoice|receipt|bill|cash memo|gst|gstin|original|duplicate|copy|phone|ph|tel|""" +
            """mobile|date|time|order|token|table|cashier|transaction|successful|completed|payment|""" +
            """contribuinte|nif|vat|produtor|steuer|siret)\b""",
        RegexOption.IGNORE_CASE,
    )

    // The first line near the top that reads like a name: mostly letters, not a heading, phone
    // number or date. "Welcome to Cafe Aroma" gives "Cafe Aroma".
    internal fun findMerchant(lines: List<String>): String? = lines.take(MERCHANT_LINES).firstNotNullOfOrNull { line ->
        val name = line.replace(WELCOME, "").trim()
        val letters = name.count { it.isLetter() }
        val looksLikeName = letters >= 3 && letters > name.length / 2 &&
            !NOT_A_MERCHANT.containsMatchIn(name)
        if (looksLikeName) cleanName(name) else null
    }

    private val WELCOME = Regex("""^\s*welcome\s+to\s+""", RegexOption.IGNORE_CASE)

    private const val MERCHANT_LINES = 5

    // endregion

    // region account

    // "XXXXXXXX1234", "**1234", "A/c XX1234", "card ending 1234", "a/c no. 1234"
    private val ACCOUNT_DIGITS = Regex(
        """(?:[x*•]{2,}|\bending(?:\s+(?:with|in))?|\b(?:a/c|account|acct?)(?:\s*no\.?)?)""" +
            """\s*[x*•]*\s*(\d{4})\b""",
        RegexOption.IGNORE_CASE,
    )

    internal fun findAccountDigits(text: String): List<String> =
        ACCOUNT_DIGITS.findAll(text).map { it.groupValues[1] }.distinct().toList()

    // endregion

    // region date

    private val MONTHS = listOf("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec")

    // "7 Nov 2026", "07 November 2026, 7:15 PM", "7 Nov 2026 at 19:15", "08-Nov-2026"
    private val TEXT_DATE = Regex(
        """\b(\d{1,2})[\s-]+(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*,?[\s-]+(\d{4})""" +
            """(?:[,\s]+(?:at\s+)?(\d{1,2}):(\d{2})\s*([ap]\.?m\.?)?)?""",
        RegexOption.IGNORE_CASE,
    )

    // "07/11/2026", "7-11-26", "08.11.2026" (day first, as in India), optionally "19:15"
    private val NUMERIC_DATE = Regex(
        """\b(\d{1,2})[/.-](\d{1,2})[/.-](\d{2,4})(?:[,\s]+(?:at\s+)?(\d{1,2}):(\d{2})\s*([ap]\.?m\.?)?)?""",
        RegexOption.IGNORE_CASE,
    )

    internal fun findDate(text: String, now: Date, zone: ZoneId): Date? {
        val dateTime = TEXT_DATE.find(text)?.let { match ->
            val (day, month, year) = match.destructured
            val monthNumber = MONTHS.indexOf(month.lowercase(Locale.ROOT).take(3)) + 1
            build(year.toInt(), monthNumber, day.toInt(), match.groupValues)
        } ?: NUMERIC_DATE.find(text)?.let { match ->
            val (day, month, year) = match.destructured
            val fullYear = year.toInt().let { if (it < 100) 2000 + it else it }
            build(fullYear, month.toInt(), day.toInt(), match.groupValues)
        } ?: return null
        val date = Date.from(dateTime.atZone(zone).toInstant())
        // A receipt is never from the future; that's a misread.
        return date.takeIf { !it.after(now) }
    }

    private fun build(year: Int, month: Int, day: Int, groups: List<String>): LocalDateTime? = runCatching {
        val hourText = groups[4]
        val time = if (hourText.isEmpty()) {
            LocalTime.NOON
        } else {
            var hour = hourText.toInt()
            val minute = groups[5].toInt()
            val meridiem = groups[6].lowercase(Locale.ROOT).replace(".", "")
            if (meridiem == "pm" && hour < 12) hour += 12
            if (meridiem == "am" && hour == 12) hour = 0
            LocalTime.of(hour, minute)
        }
        LocalDateTime.of(LocalDate.of(year, month, day), time)
    }.getOrNull()

    // endregion
}
