package com.naveenapps.expensemanager.shared

import com.naveenapps.expensemanager.core.domain.usecase.receipt.ReceiptParser
import com.naveenapps.expensemanager.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/**
 * The Swift entry point returns exactly what the shared parser found, on the JVM and on iOS
 * (OpenSpec change add-ios-smoke-app, task 1.4). Samples as in ReceiptParserTest.
 */
class ReceiptReaderTest {

    private val zone = TimeZone.UTC
    private val now = LocalDateTime(2026, 11, 8, 10, 0).toInstant(zone)

    private val gpay = """
        ₹250
        Paid to
        SWIGGY
        swiggy.in@icici
        Completed
        7 Nov 2026, 7:15 pm
        UPI transaction ID
        631234567890
        From: Naveen Kumar (HDFC Bank)
    """.trimIndent()

    private val bankCredit = "Rs 5,000.00 credited to your A/c XX1234 on 06-11-26 by UPI from ARUN K. Avl Bal Rs 12,345.67"

    private val shopBill = """
        FRESH MART SUPERMARKET
        Tax Invoice
        Date: 05/11/2026 18:42
        Bread               45.00
        Sub Total          101.00
        Grand Total        106.05
        Card **** 4821
    """.trimIndent()

    @Test
    fun everyFieldMatchesTheSharedParser() {
        listOf(gpay, bankCredit, shopBill, "no receipt here").forEach { text ->
            val parsed = ReceiptParser.parse(text, now, zone)
            val summary = ReceiptReader.read(text, now, zone)

            assertEquals(parsed.amount, summary.amount, text)
            assertEquals(parsed.payee, summary.payee, text)
            assertEquals(parsed.type == TransactionType.INCOME, summary.isIncome, text)
            assertEquals(parsed.date?.toEpochMilliseconds(), summary.dateEpochMillis, text)
            assertEquals(parsed.accountDigits, summary.accountDigits, text)
        }
    }

    @Test
    fun gpayMessageAsSwiftSeesIt() {
        val summary = ReceiptReader.read(gpay, now, zone)

        assertEquals(250.0, summary.amount)
        assertEquals("SWIGGY", summary.payee)
        assertFalse(summary.isIncome)
        assertEquals(LocalDateTime(2026, 11, 7, 19, 15).toInstant(zone).toEpochMilliseconds(), summary.dateEpochMillis)
    }

    @Test
    fun bankCreditIsIncomeAndShopBillKeepsTheCardDigits() {
        assertTrue(ReceiptReader.read(bankCredit, now, zone).isIncome)
        assertEquals(listOf("4821"), ReceiptReader.read(shopBill, now, zone).accountDigits)
    }
}
