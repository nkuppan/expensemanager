package com.naveenapps.expensemanager.core.domain.usecase.receipt

import com.naveenapps.expensemanager.core.domain.assertContainsExactly
import com.naveenapps.expensemanager.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class ReceiptParserTest {

    private val now = LocalDateTime(2026, 11, 8, 10, 0).toInstant(TimeZone.UTC)

    private fun parse(text: String) = ReceiptParser.parse(text.trimIndent(), now, TimeZone.UTC)

    private fun Instant.local() = toLocalDateTime(TimeZone.UTC)

    @Test
    fun `GPay screenshot text`() {
        val receipt = parse(
            """
            ₹250
            Paid to
            SWIGGY
            swiggy.in@icici
            Completed
            7 Nov 2026, 7:15 pm
            UPI transaction ID
            631234567890
            From: Naveen Kumar (HDFC Bank)
            """,
        )

        assertEquals(250.0, receipt.amount)
        assertEquals("SWIGGY", receipt.payee)
        assertEquals(TransactionType.EXPENSE, receipt.type)
        assertEquals(LocalDateTime(2026, 11, 7, 19, 15), receipt.date?.local())
    }

    @Test
    fun `PhonePe screenshot text with a grouped amount`() {
        val receipt = parse(
            """
            Transaction Successful
            07:15 pm on 07 Nov 2026
            Paid to
            Ramesh Kirana Store
            9876543210@ybl
            ₹ 1,240
            Debited from
            XXXXXXXX1234
            UTR: 431234567890
            """,
        )

        assertEquals(1240.0, receipt.amount)
        assertEquals("Ramesh Kirana Store", receipt.payee)
        assertEquals("2026-11-07", receipt.date?.local()?.date.toString())
    }

    @Test
    fun `Paytm shared text in one sentence`() {
        val receipt = parse("Paid Rs.120 to Chai Point on 07-11-2026 at 18:05. UPI Ref No 431234567890. - Paytm")

        assertEquals(120.0, receipt.amount)
        assertEquals("Chai Point", receipt.payee)
        assertEquals(LocalDateTime(2026, 11, 7, 18, 5), receipt.date?.local())
    }

    @Test
    fun `bank credit message is income and skips the balance`() {
        val receipt = parse(
            "Rs 5,000.00 credited to your A/c XX1234 on 06-11-26 by UPI from ARUN K. Avl Bal Rs 12,345.67",
        )

        assertEquals(TransactionType.INCOME, receipt.type)
        assertEquals(5000.0, receipt.amount)
        assertEquals("ARUN K", receipt.payee)
        assertEquals("2026-11-06", receipt.date?.local()?.date.toString())
    }

    @Test
    fun `a bare number line is used when the rupee sign wasn't recognised`() {
        val receipt = parse(
            """
            Paid to
            Metro Card Recharge
            500
            UPI transaction ID
            631234567890
            """,
        )

        assertEquals(500.0, receipt.amount)
        assertEquals("Metro Card Recharge", receipt.payee)
    }

    @Test
    fun `a UPI ID alone becomes its readable handle`() {
        assertEquals("chaiwala", parse("Paid to\nchaiwala@okaxis\n₹20").payee)
    }

    @Test
    fun `a date in the future is treated as a misread`() {
        assertNull(parse("Paid ₹99 to Netflix on 10 Dec 2026").date)
    }

    @Test
    fun `text with no amount gives no amount`() {
        val receipt = parse("Hello, here is the link to the app")

        assertNull(receipt.amount)
        assertNull(receipt.payee)
    }

    @Test
    fun `photo of a shop bill uses the grand total and the shop name`() {
        val receipt = parse(
            """
            FRESH MART SUPERMARKET
            Tax Invoice
            GSTIN 33ABCDE1234F1Z5
            Date: 05/11/2026 18:42
            Milk 1L        2   56.00
            Bread               45.00
            Sub Total          101.00
            CGST 2.5%            2.53
            SGST 2.5%            2.52
            Total Qty: 3
            Grand Total        106.05
            Card **** 4821
            """,
        )

        assertEquals(106.05, receipt.amount)
        assertEquals("FRESH MART SUPERMARKET", receipt.payee)
        assertEquals(TransactionType.EXPENSE, receipt.type)
        assertEquals(LocalDateTime(2026, 11, 5, 18, 42), receipt.date?.local())
        assertContainsExactly(listOf("4821"), receipt.accountDigits)
    }

    @Test
    fun `a dashed text date on a bill is read`() {
        val receipt = parse("Cafe Aroma\n07-Nov-2026\nTOTAL 240")

        assertEquals(240.0, receipt.amount)
        assertEquals("Cafe Aroma", receipt.payee)
        assertEquals("2026-11-07", receipt.date?.local()?.date.toString())
    }

    @Test
    fun `masked account and card numbers give their last digits`() {
        assertContainsExactly(listOf("1234"), parse("Rs 500 debited from A/c XX1234 on 06-11-26").accountDigits)
        assertContainsExactly(listOf("9876"), parse("Debited from\nXXXXXXXX9876\n₹ 300").accountDigits)
        assertContainsExactly(listOf("4321"), parse("Paid ₹99 using card ending 4321").accountDigits)
        assertTrue(parse("Paid to\nchaiwala@okaxis\n₹20").accountDigits.isEmpty())
    }

    @Test
    fun `the shop name skips headings and a welcome line`() {
        assertEquals("Hotel Saravana Bhavan", parse("TAX INVOICE\nWelcome to Hotel Saravana Bhavan\nPh: 044 2345 6789\nNet Payable  320").payee)
    }

    // The receipt from the field report (a Lidl bill from Portugal), as ReceiptRows rebuilds it.
    @Test
    fun `a European bill with decimal commas reads the total and shop and date`() {
        val receipt = parse(
            """
            LIDL
            V.REAL STO ANTONIO
            Contribuinte Nr. 503 340 855
            Produtor de REEE Nr. PT000048
            EUR
            TOMATE DE CACHO  0,80 B
            0,540 kg x 1,49  EUR/kg
            ABACAXI  1,33 B
            1,345 kg x 0,99  EUR/kg
            BRANCO REG RIBATEJO  2,58 C
            2 x 1,29
            Total  19,96
            DINHEIRO  20,00
            TROCO  -0,04
            Taxa  Base Imp.  Val.Total  Val.IVA
            B 6%  14,52  15,39  0,87
            C 13%  4,04  4,57  0,53
            0345  024361/07  17.08.10  12:14
            ***** OBRIGADO *****
            """,
        )

        assertEquals(19.96, receipt.amount)
        assertEquals("LIDL", receipt.payee)
        assertEquals(LocalDateTime(2010, 8, 17, 12, 14), receipt.date?.local())
        assertTrue(receipt.accountDigits.isEmpty())
    }

    @Test
    fun `when the logo isn't read the shop line under it is used`() {
        assertEquals("V.REAL STO ANTONIO", parse("V.REAL STO ANTONIO\nContribuinte Nr. 503 340 855\nTotal  19,96").payee)
    }

    @Test
    fun `currency symbols before or after the number`() {
        assertEquals(12.5, parse("Total: € 12,50").amount)
        assertEquals(8.4, parse("Paid 8,40 € at Cafe").amount)
        assertEquals(1234.56, parse("Amount \$ 1,234.56").amount)
        assertEquals(1234.56, parse("Summe EUR 1.234,56").amount)
    }

    @Test
    fun `numbers in every separator style`() {
        assertEquals(100000.0, ReceiptParser.parseNumber("1,00,000"))
        assertEquals(1240.0, ReceiptParser.parseNumber("1,240"))
        assertEquals(1234.56, ReceiptParser.parseNumber("1,234.56"))
        assertEquals(1234.56, ReceiptParser.parseNumber("1.234,56"))
        assertEquals(19.96, ReceiptParser.parseNumber("19,96"))
        assertEquals(0.54, ReceiptParser.parseNumber("0,540"))
        assertEquals(250.0, ReceiptParser.parseNumber("250"))
    }
}
