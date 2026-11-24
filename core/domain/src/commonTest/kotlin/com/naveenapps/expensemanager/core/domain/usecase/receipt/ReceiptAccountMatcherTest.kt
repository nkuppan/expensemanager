package com.naveenapps.expensemanager.core.domain.usecase.receipt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReceiptAccountMatcherTest {

    private val accounts = listOf("Cash", "HDFC Savings", "ICICI Credit Card 4821", "SBI Salary Account")

    private fun match(text: String, payee: String? = null) = ReceiptAccountMatcher.match(
        receiptText = text,
        payee = payee,
        accountDigits = ReceiptParser.findAccountDigits(text),
        accounts = accounts,
        name = { it },
    )

    @Test
    fun `last digits on the receipt pick the account named with them`() {
        assertEquals("ICICI Credit Card 4821", match("Grand Total 106.05\nCard **** 4821"))
    }

    @Test
    fun `the bank's name on the receipt picks its account`() {
        assertEquals("HDFC Savings", match("Paid to\nSWIGGY\nFrom: Naveen Kumar (HDFC Bank)"))
    }

    @Test
    fun `generic words like bank or card don't match`() {
        assertNull(match("Paid by card at the bank counter"))
    }

    @Test
    fun `the payee's own name is not taken as the account`() {
        assertNull(match("Paid to SBI Life Insurance", payee = "SBI Life Insurance"))
    }

    @Test
    fun `two equally good matches give no guess`() {
        assertNull(match("Transferred from HDFC to SBI"))
    }
}
