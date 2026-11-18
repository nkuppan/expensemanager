package com.naveenapps.expensemanager.core.domain.usecase.receipt

import com.google.common.truth.Truth.assertThat
import org.junit.Test

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
        assertThat(match("Grand Total 106.05\nCard **** 4821")).isEqualTo("ICICI Credit Card 4821")
    }

    @Test
    fun `the bank's name on the receipt picks its account`() {
        assertThat(match("Paid to\nSWIGGY\nFrom: Naveen Kumar (HDFC Bank)")).isEqualTo("HDFC Savings")
    }

    @Test
    fun `generic words like bank or card don't match`() {
        assertThat(match("Paid by card at the bank counter")).isNull()
    }

    @Test
    fun `the payee's own name is not taken as the account`() {
        assertThat(match("Paid to SBI Life Insurance", payee = "SBI Life Insurance")).isNull()
    }

    @Test
    fun `two equally good matches give no guess`() {
        assertThat(match("Transferred from HDFC to SBI")).isNull()
    }
}
