package com.naveenapps.expensemanager.core.domain.usecase.receipt

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.TransactionType
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date
import org.junit.Test

class ReceiptParserTest {

    private val zone = ZoneOffset.UTC
    private val now = Date.from(LocalDateTime.of(2026, 11, 8, 10, 0).toInstant(zone))

    private fun parse(text: String) = ReceiptParser.parse(text.trimIndent(), now, zone)

    private fun Date.local() = toInstant().atZone(zone).toLocalDateTime()

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

        assertThat(receipt.amount).isEqualTo(250.0)
        assertThat(receipt.payee).isEqualTo("SWIGGY")
        assertThat(receipt.type).isEqualTo(TransactionType.EXPENSE)
        assertThat(receipt.date?.local()).isEqualTo(LocalDateTime.of(2026, 11, 7, 19, 15))
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

        assertThat(receipt.amount).isEqualTo(1240.0)
        assertThat(receipt.payee).isEqualTo("Ramesh Kirana Store")
        assertThat(receipt.date?.local()?.toLocalDate().toString()).isEqualTo("2026-11-07")
    }

    @Test
    fun `Paytm shared text in one sentence`() {
        val receipt = parse("Paid Rs.120 to Chai Point on 07-11-2026 at 18:05. UPI Ref No 431234567890. - Paytm")

        assertThat(receipt.amount).isEqualTo(120.0)
        assertThat(receipt.payee).isEqualTo("Chai Point")
        assertThat(receipt.date?.local()).isEqualTo(LocalDateTime.of(2026, 11, 7, 18, 5))
    }

    @Test
    fun `bank credit message is income and skips the balance`() {
        val receipt = parse(
            "Rs 5,000.00 credited to your A/c XX1234 on 06-11-26 by UPI from ARUN K. Avl Bal Rs 12,345.67",
        )

        assertThat(receipt.type).isEqualTo(TransactionType.INCOME)
        assertThat(receipt.amount).isEqualTo(5000.0)
        assertThat(receipt.payee).isEqualTo("ARUN K")
        assertThat(receipt.date?.local()?.toLocalDate().toString()).isEqualTo("2026-11-06")
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

        assertThat(receipt.amount).isEqualTo(500.0)
        assertThat(receipt.payee).isEqualTo("Metro Card Recharge")
    }

    @Test
    fun `a UPI ID alone becomes its readable handle`() {
        assertThat(parse("Paid to\nchaiwala@okaxis\n₹20").payee).isEqualTo("chaiwala")
    }

    @Test
    fun `a date in the future is treated as a misread`() {
        assertThat(parse("Paid ₹99 to Netflix on 10 Dec 2026").date).isNull()
    }

    @Test
    fun `text with no amount gives no amount`() {
        val receipt = parse("Hello, here is the link to the app")

        assertThat(receipt.amount).isNull()
        assertThat(receipt.payee).isNull()
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

        assertThat(receipt.amount).isEqualTo(106.05)
        assertThat(receipt.payee).isEqualTo("FRESH MART SUPERMARKET")
        assertThat(receipt.type).isEqualTo(TransactionType.EXPENSE)
        assertThat(receipt.date?.local()).isEqualTo(LocalDateTime.of(2026, 11, 5, 18, 42))
        assertThat(receipt.accountDigits).containsExactly("4821")
    }

    @Test
    fun `a dashed text date on a bill is read`() {
        val receipt = parse("Cafe Aroma\n07-Nov-2026\nTOTAL 240")

        assertThat(receipt.amount).isEqualTo(240.0)
        assertThat(receipt.payee).isEqualTo("Cafe Aroma")
        assertThat(receipt.date?.local()?.toLocalDate().toString()).isEqualTo("2026-11-07")
    }

    @Test
    fun `masked account and card numbers give their last digits`() {
        assertThat(parse("Rs 500 debited from A/c XX1234 on 06-11-26").accountDigits).containsExactly("1234")
        assertThat(parse("Debited from\nXXXXXXXX9876\n₹ 300").accountDigits).containsExactly("9876")
        assertThat(parse("Paid ₹99 using card ending 4321").accountDigits).containsExactly("4321")
        assertThat(parse("Paid to\nchaiwala@okaxis\n₹20").accountDigits).isEmpty()
    }

    @Test
    fun `the shop name skips headings and a welcome line`() {
        assertThat(parse("TAX INVOICE\nWelcome to Hotel Saravana Bhavan\nPh: 044 2345 6789\nNet Payable  320").payee)
            .isEqualTo("Hotel Saravana Bhavan")
    }

    // The receipt from the field report (a Lidl bill from Portugal), as ReceiptRows rebuilds it.
    @Test
    fun `a European bill with decimal commas reads the total, shop and date`() {
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

        assertThat(receipt.amount).isEqualTo(19.96)
        assertThat(receipt.payee).isEqualTo("LIDL")
        assertThat(receipt.date?.local()).isEqualTo(LocalDateTime.of(2010, 8, 17, 12, 14))
        assertThat(receipt.accountDigits).isEmpty()
    }

    @Test
    fun `when the logo isn't read the shop line under it is used`() {
        assertThat(parse("V.REAL STO ANTONIO\nContribuinte Nr. 503 340 855\nTotal  19,96").payee)
            .isEqualTo("V.REAL STO ANTONIO")
    }

    @Test
    fun `currency symbols before or after the number`() {
        assertThat(parse("Total: € 12,50").amount).isEqualTo(12.5)
        assertThat(parse("Paid 8,40 € at Cafe").amount).isEqualTo(8.4)
        assertThat(parse("Amount \$ 1,234.56").amount).isEqualTo(1234.56)
        assertThat(parse("Summe EUR 1.234,56").amount).isEqualTo(1234.56)
    }

    @Test
    fun `numbers in every separator style`() {
        assertThat(ReceiptParser.parseNumber("1,00,000")).isEqualTo(100000.0)
        assertThat(ReceiptParser.parseNumber("1,240")).isEqualTo(1240.0)
        assertThat(ReceiptParser.parseNumber("1,234.56")).isEqualTo(1234.56)
        assertThat(ReceiptParser.parseNumber("1.234,56")).isEqualTo(1234.56)
        assertThat(ReceiptParser.parseNumber("19,96")).isEqualTo(19.96)
        assertThat(ReceiptParser.parseNumber("0,540")).isEqualTo(0.54)
        assertThat(ReceiptParser.parseNumber("250")).isEqualTo(250.0)
    }
}
