package com.naveenapps.expensemanager.feature.transaction.import.parser

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/**
 * Fixtures reproduce the column layout, indentation and wrapping of a real
 * statement - including the truncated description column and the transfer marker
 * printed above its row - but every name, account number, reference, amount and
 * date here is generated, not taken from a statement.
 */
class SbiYonoStatementParserTest {

    private val parser = SbiYonoStatementParser()

    private val statementHeader = """
        STATEMENT OF ACCOUNT
        State Bank of India
        Balance
    """.trimIndent()

    private val withdrawalRow = """
        WDL TFR
        01/06/2025   01/06/2025   UPI/DR/800000000001/ALPHA MART         -       111.11         -      12,34,567.89
                                   /TESTBNK/MOCKSHOP/MOCK R
                                   0000000000 AT 00000 TESTBR
    """.trimIndent()

    private val depositRow = """
        DEP TFR
        01/06/2025   01/06/2025   UPI/CR/800000000002/BETA STORE         -          -         222.22   12,34,790.11
                                   /TESTBNK/MOCKPAY/MOCK P
                                   0000000001 AT 00000 TESTBR
    """.trimIndent()

    private fun startOfDay(date: String) = LocalDate.parse(date)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    @Test
    fun `parses withdrawal row with wrapped description`() {
        val result = parser.parse("$statementHeader\n$withdrawalRow")

        assertThat(result).hasSize(1)
        val tx = result.first()
        assertThat(tx.amount).isEqualTo(111.11)
        assertThat(tx.transactionType).isEqualTo(TransactionType.EXPENSE)
        assertThat(tx.payOrCollect).isEqualTo("PAY")
        assertThat(tx.referenceId).isEqualTo("800000000001")
        assertThat(tx.receiverName).isEqualTo("ALPHA MART")
        assertThat(tx.senderName).isEmpty()
        assertThat(tx.bankName).isEqualTo("State Bank of India")
        assertThat(tx.status).isEqualTo("SUCCESS")
        assertThat(tx.isSuccess).isTrue()
        assertThat(tx.parseError).isNull()
    }

    @Test
    fun `parses deposit row as income`() {
        val result = parser.parse("$statementHeader\n$depositRow")

        assertThat(result).hasSize(1)
        val tx = result.first()
        assertThat(tx.amount).isEqualTo(222.22)
        assertThat(tx.transactionType).isEqualTo(TransactionType.INCOME)
        assertThat(tx.payOrCollect).isEqualTo("COLLECT")
        assertThat(tx.senderName).isEqualTo("BETA STORE")
        assertThat(tx.receiverName).isEmpty()
        assertThat(tx.referenceId).isEqualTo("800000000002")
    }

    @Test
    fun `row date is start of day and flagged date only`() {
        val result = parser.parse("$statementHeader\n$withdrawalRow")

        val tx = result.first()
        assertThat(tx.dateTime.time).isEqualTo(startOfDay("2025-06-01"))
        assertThat(tx.isDateOnly).isTrue()
    }

    @Test
    fun `keeps the transfer marker out of the previous description`() {
        val text = "$statementHeader\n$withdrawalRow\n$depositRow"
        val result = parser.parse(text)

        assertThat(result).hasSize(2)
        assertThat(result[0].rawText).doesNotContain("TFR")
        assertThat(result[1].rawText).doesNotContain("TFR")
    }

    @Test
    fun `description wraps stay with their own row`() {
        val text = "$statementHeader\n$withdrawalRow\n$depositRow"
        val result = parser.parse(text)

        assertThat(result[0].rawText).contains("0000000000 AT 00000 TESTBR")
        assertThat(result[0].rawText).doesNotContain("BETA STORE")
        assertThat(result[1].rawText).contains("0000000001 AT 00000 TESTBR")
    }

    @Test
    fun `parses indian grouped amounts`() {
        val row = "WDL TFR\n" +
            "04/06/2025   04/06/2025   UPI/DR/800000000003/GAMMA MART          -      20,000.00      -       1,00,007.00\n" +
            "                                   /TESTBNK/MOCKPAY/MOCK R\n" +
            "                                   0000000002 AT 00000 TESTBR"

        val result = parser.parse("$statementHeader\n$row")

        assertThat(result).hasSize(1)
        assertThat(result.first().amount).isEqualTo(20000.00)
    }

    @Test
    fun `transfer marker decides direction when description ends with a dash`() {
        val row = "WDL TFR\n" +
            "05/06/2025   05/06/2025   ATM WDL -                            -       777.77         -       1,00,006.00"

        val result = parser.parse("$statementHeader\n$row")

        assertThat(result).hasSize(1)
        val tx = result.first()
        assertThat(tx.amount).isEqualTo(777.77)
        assertThat(tx.transactionType).isEqualTo(TransactionType.EXPENSE)
    }

    @Test
    fun `parses neft row beneficiary and urn`() {
        val row = "DEP TFR\n" +
            "09/06/2025   09/06/2025   NEFT*TESTBNK01*TESTREF001          -          -       1,500.00   1,00,008.00\n" +
            "                                   000001*MOCK VENDOR LTD*T\n" +
            "                                   0000000003 AT 00000 TESTBR"

        val result = parser.parse("$statementHeader\n$row")

        assertThat(result).hasSize(1)
        val tx = result.first()
        assertThat(tx.amount).isEqualTo(1500.00)
        assertThat(tx.transactionType).isEqualTo(TransactionType.INCOME)
        assertThat(tx.senderName).isEqualTo("MOCK VENDOR LTD")
        assertThat(tx.referenceId).isEqualTo("TESTBNK01*TESTREF001")
    }

    @Test
    fun `row without transfer marker is read from the money columns`() {
        val row = "25/06/2025   25/06/2025   INTEREST CREDIT                         -         -         555.00      1,00,005.00"

        val result = parser.parse("$statementHeader\n$row")

        assertThat(result).hasSize(1)
        val tx = result.first()
        assertThat(tx.amount).isEqualTo(555.00)
        assertThat(tx.transactionType).isEqualTo(TransactionType.INCOME)
        assertThat(tx.senderName).isEqualTo("INTEREST CREDIT")
        assertThat(tx.referenceId).isEmpty()
    }

    @Test
    fun `non transfer credit is read from the description`() {
        val row = "06/07/2025   06/07/2025   MOCKBANK0001234                    -          -        66.00      3,10,965.46\n" +
            "                                   SAMPLE DIV 2025"

        val result = parser.parse("$statementHeader\n$row")

        assertThat(result).hasSize(1)
        val tx = result.first()
        assertThat(tx.amount).isEqualTo(66.00)
        assertThat(tx.transactionType).isEqualTo(TransactionType.INCOME)
        assertThat(tx.senderName).contains("SAMPLE DIV")
    }

    @Test
    fun `skips page headers footers and balance captions`() {
        val text = listOf(
            statementHeader,
            withdrawalRow,
            "",
            "Page no. 1",
            "                                    Balance",
            "",
            depositRow,
            "                                   ( )                                           ( )             ( )               ( )",
            "Page no. 2",
        ).joinToString("\n")

        val result = parser.parse(text)

        assertThat(result).hasSize(2)
        assertThat(result.map { it.referenceId })
            .containsExactly("800000000001", "800000000002").inOrder()
    }

    @Test
    fun `holder account number is read from the header when present`() {
        val header = "$statementHeader\nA/c No. XXXXXX0001\nBranch: TESTBR"
        val result = parser.parse("$header\n$withdrawalRow")

        assertThat(result.first().accountNumber).isEqualTo("XXXXXX0001")
    }

    @Test
    fun `holder account number is N slash A when the statement omits it`() {
        val result = parser.parse("$statementHeader\n$withdrawalRow")

        assertThat(result.first().accountNumber).isEqualTo("N/A")
    }

    @Test
    fun `counterparty vpa is unknown for a statement layout`() {
        val result = parser.parse("$statementHeader\n$withdrawalRow")

        val tx = result.first()
        assertThat(tx.senderVpa).isEqualTo("N/A")
        assertThat(tx.receiverVpa).isEqualTo("N/A")
        assertThat(tx.defaultNotes).isEqualTo("ALPHA MART Ref:800000000001 PAY")
    }

    @Test
    fun `impossible date is flagged invalid instead of dropped`() {
        val row = "WDL TFR\n" +
            "31/02/2025   31/02/2025   UPI/DR/800000000001/ALPHA MART        -       111.11         -      12,34,567.89"

        val result = parser.parse("$statementHeader\n$row")

        assertThat(result).hasSize(1)
        assertThat(result.first().parseError).isNotNull()
    }

    @Test
    fun `line without a money tail is not a transaction`() {
        val text = "$statementHeader\n01/06/2025   01/06/2025   UPI/DR/800000000001/ALPHA MART"

        assertThat(parser.parse(text)).isEmpty()
    }

    @Test
    fun `blank text yields nothing`() {
        assertThat(parser.parse("   \n  ")).isEmpty()
    }

    @Test
    fun `canHandle detects a yono account statement`() {
        assertThat(parser.canHandle("$statementHeader\n$withdrawalRow")).isTrue()
    }

    @Test
    fun `canHandle rejects a bhim transaction history`() {
        val bhim = "Transaction History\nDate Time Bank Name Payment ID/Reference Number Pay/Collect"

        assertThat(parser.canHandle(bhim)).isFalse()
    }

    @Test
    fun `canHandle requires the statement header`() {
        assertThat(parser.canHandle(withdrawalRow)).isFalse()
    }

    @Test
    fun `concurrent parsing yields correct dates`() = runBlocking {
        val text = "$statementHeader\n$withdrawalRow"
        val expected = startOfDay("2025-06-01")

        val results = (1..16).map {
            async(Dispatchers.Default) { parser.parse(text) }
        }.awaitAll()

        results.forEach { parsed ->
            assertThat(parsed).hasSize(1)
            assertThat(parsed.first().dateTime.time).isEqualTo(expected)
        }
    }
}
