package com.naveenapps.expensemanager.shared

import com.naveenapps.expensemanager.core.domain.usecase.receipt.ReceiptParser
import com.naveenapps.expensemanager.core.model.TransactionType
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

/**
 * What [ReceiptReader] found in a receipt, as plain values Swift can use directly. Any part may be
 * missing. Swift formats them (amount, date in the device's zone) for display.
 */
data class ReceiptSummary(
    val amount: Double?,
    val payee: String?,
    val isIncome: Boolean,
    /** The receipt's date and time, as epoch milliseconds; null if none was found. */
    val dateEpochMillis: Long?,
    /** Last digits of the card or account it was paid from. */
    val accountDigits: List<String>,
)

/**
 * Swift entry point to the shared receipt parser (`ReceiptReader.shared.read(text:)`). It only
 * adapts [ReceiptParser]'s result to plain values: Swift can't pass Kotlin default arguments, and
 * `Instant` and the model's enums would reach Swift as awkward types.
 */
object ReceiptReader {

    fun read(text: String): ReceiptSummary = read(text, Clock.System.now(), TimeZone.currentSystemDefault())

    /** [read] with a fixed clock and zone, for tests. */
    internal fun read(text: String, now: Instant, zone: TimeZone): ReceiptSummary {
        val parsed = ReceiptParser.parse(text, now, zone)
        return ReceiptSummary(
            amount = parsed.amount,
            payee = parsed.payee,
            isIncome = parsed.type == TransactionType.INCOME,
            dateEpochMillis = parsed.date?.toEpochMilliseconds(),
            accountDigits = parsed.accountDigits,
        )
    }
}
