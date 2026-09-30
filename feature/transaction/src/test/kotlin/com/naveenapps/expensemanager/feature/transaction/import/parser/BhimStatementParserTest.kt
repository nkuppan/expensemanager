package com.naveenapps.expensemanager.feature.transaction.import.parser

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Fixtures are synthetic. The column layout, padding and delimiter quirks mirror
 * a real statement so the parser is exercised faithfully, but no name, account
 * number, reference or amount here came from an actual statement.
 */
class BhimStatementParserTest {

    private val parser = BhimStatementParser()

    private val sampleHeader = """
        Transaction History
        Customer Mobile Number: +91XXXXXXXXX  Transaction History from 24/06/2025 to 24/09/2025
        Date       Time             Bank Name           Account Number      Sender      Receiver      Payment ID/Reference Number   Pay/Collect   Amount (in Rs.)    DR/CR        Status
    """.trimIndent()

    @Test
    fun `parses debit expense row`() {
        val line = "23/09/2025   01:38:31       State Bank Of India     XXXXXX0001                  mockholder01@upi(MOCK HOLDER)                       mockshop02@oksbi(MOCK SHOP)                  900000000001              PAY             111.11          DR       SUCCESS"
        val result = parser.parse("$sampleHeader\n$line")

        assertThat(result).hasSize(1)
        val tx = result.first()
        assertThat(tx.amount).isEqualTo(111.11)
        assertThat(tx.transactionType).isEqualTo(TransactionType.EXPENSE)
        assertThat(tx.referenceId).isEqualTo("900000000001")
        assertThat(tx.senderVpa).isEqualTo("mockholder01@upi")
        assertThat(tx.senderName).isEqualTo("MOCK HOLDER")
        assertThat(tx.receiverVpa).isEqualTo("mockshop02@oksbi")
        assertThat(tx.bankName).isEqualTo("State Bank Of India")
        assertThat(tx.accountNumber).isEqualTo("XXXXXX0001")
        assertThat(tx.status).isEqualTo("SUCCESS")
        assertThat(tx.isSuccess).isTrue()
        assertThat(tx.parseError).isNull()
    }

    @Test
    fun `parses credit income row`() {
        val line = "21/09/2025   01:06:35       State Bank Of India     XXXXXX0001             mockpay04@upi(MOCK PAYMENTS)                            mockholder01@upi(MOCK HOLDER)                           900000000002              PAY             22.22            CR      SUCCESS"
        val result = parser.parse(line)

        assertThat(result).hasSize(1)
        val tx = result.first()
        assertThat(tx.amount).isEqualTo(22.22)
        assertThat(tx.transactionType).isEqualTo(TransactionType.INCOME)
        assertThat(tx.counterpartyVpa).isEqualTo("mockpay04@upi")
    }

    @Test
    fun `parses collect row and a second bank account`() {
        val line = "20/09/2025   04:45:52   MOCK BANK CREDIT CARD    000000XXXXXX00   mockholder01@upi(MOCK HOLDER)            mockapp05@mockbank(MOCK APP)           900000000003           COLLECT             2.22           DR       SUCCESS"
        val result = parser.parse(line)

        assertThat(result).hasSize(1)
        val tx = result.first()
        assertThat(tx.payOrCollect).isEqualTo("COLLECT")
        assertThat(tx.accountNumber).isEqualTo("000000XXXXXX00")
        assertThat(tx.receiverName).isEqualTo("MOCK APP")
    }

    @Test
    fun `parses N-A receiver as missing counterparty`() {
        val line = "31/08/2025   02:33:44       State Bank Of India     XXXXXX0001                  mockholder01@upi(MOCK HOLDER)                                     N/AN/A                                900000000004              PAY            3333.33          DR       SUCCESS"
        val result = parser.parse(line)

        assertThat(result).hasSize(1)
        val tx = result.first()
        assertThat(tx.amount).isEqualTo(3333.33)
        assertThat(tx.receiverVpa).isEqualTo("N/A")
        assertThat(tx.referenceId).isEqualTo("900000000004")
    }

    @Test
    fun `keeps failure status for user review`() {
        val line = "04/09/2025   21:13:42   MOCK BANK CREDIT CARD    000000XXXXXX00              mockholder01@upi(MOCK HOLDER)                      mockbiller06@rapl(MOCK RAIL)                  900000000005              PAY            1500.50          DR       FAILURE"
        val result = parser.parse(line)

        assertThat(result).hasSize(1)
        assertThat(result.first().isSuccess).isFalse()
        assertThat(result.first().status).isEqualTo("FAILURE")
    }

    @Test
    fun `parses five bank-failed rows for review`() {
        val lines = (1..5).joinToString("\n") { index ->
            "04/09/2025   21:13:42   MOCK BANK CREDIT CARD    000000XXXXXX00              mockholder01@upi(MOCK HOLDER)                      mockbiller06@rapl(MOCK RAIL)                  90000000001$index              PAY            1500.50          DR       FAILURE"
        }

        val result = parser.parse(lines)

        assertThat(result).hasSize(5)
        assertThat(result.map { it.status }).containsExactly("FAILURE", "FAILURE", "FAILURE", "FAILURE", "FAILURE")
        assertThat(result.count { it.isSuccess }).isEqualTo(0)
    }

    @Test
    fun `parses indian grouped amount with commas`() {
        val line = "22/07/2025   16:22:47      State Bank Of India     XXXXXX0001                   mockholder01@upi(MOCK HOLDER)                   mockschool07@mockbank(MOCK SCHOOL)     900000000008   PAY   99999.99   DR   SUCCESS"
        val result = parser.parse(line)

        assertThat(result).hasSize(1)
        assertThat(result.first().amount).isEqualTo(99999.99)
    }

    @Test
    fun `skips headers and footers`() {
        val text = "$sampleHeader\nPage no. 1\n\n"
        assertThat(parser.parse(text)).isEmpty()
    }

    @Test
    fun `canHandle detects bhim statement`() {
        assertThat(parser.canHandle(sampleHeader)).isTrue()
        assertThat(parser.canHandle("random text")).isFalse()
    }

    @Test
    fun `default notes contain counterparty and ref`() {
        val line = "23/09/2025   01:38:31       State Bank Of India     XXXXXX0001                  mockholder01@upi(MOCK HOLDER)                       mockshop02@oksbi(MOCK SHOP)                  900000000001              PAY             111.11          DR       SUCCESS"
        val tx = parser.parse(line).first()
        assertThat(tx.defaultNotes).contains("900000000001")
    }

    @Test
    fun `parses single-space pdfbox layout`() {
        val line = "23/09/2025 01:38:31 State Bank Of India XXXXXX0001 mockholder01@upi(MOCK HOLDER) mockshop02@oksbi(MOCK SHOP) 900000000001 PAY 111.11 DR SUCCESS"
        val result = parser.parse(line)

        assertThat(result).hasSize(1)
        val tx = result.first()
        assertThat(tx.amount).isEqualTo(111.11)
        assertThat(tx.bankName).isEqualTo("State Bank Of India")
        assertThat(tx.accountNumber).isEqualTo("XXXXXX0001")
        assertThat(tx.senderVpa).isEqualTo("mockholder01@upi")
        assertThat(tx.receiverVpa).isEqualTo("mockshop02@oksbi")
    }

    @Test
    fun `parses single-space N-A receiver`() {
        val line = "31/08/2025 02:33:44 State Bank Of India XXXXXX0001 mockholder01@upi(MOCK HOLDER) N/AN/A 900000000004 PAY 3333.33 DR SUCCESS"
        val result = parser.parse(line)

        assertThat(result).hasSize(1)
        assertThat(result.first().receiverVpa).isEqualTo("N/A")
        assertThat(result.first().referenceId).isEqualTo("900000000004")
    }

    @Test
    fun `merchant text resembling delimiters does not misparse`() {
        val line = "16/07/2025   13:57:52      State Bank Of India     XXXXXX0001               mockholder01@upi(MOCK HOLDER)                      mockfood08@mockbank(MOCK FOOD PAY DR)                  900000000009   PAY    15.75     DR   SUCCESS"
        val result = parser.parse(line)

        assertThat(result).hasSize(1)
        val tx = result.first()
        assertThat(tx.amount).isEqualTo(15.75)
        assertThat(tx.referenceId).isEqualTo("900000000009")
        assertThat(tx.receiverName).isEqualTo("MOCK FOOD PAY DR")
    }

    @Test
    fun `malformed tail is skipped`() {
        val line = "16/07/2025   13:57:52      State Bank Of India     XXXXXX0001               mockholder01@upi(MOCK HOLDER)                      mockfood08@mockbank(MOCK FOOD)"
        assertThat(parser.parse(line)).isEmpty()
    }

    @Test
    fun `impossible date is flagged invalid`() {
        val line = "99/99/2025   01:38:31       State Bank Of India     XXXXXX0001                  mockholder01@upi(MOCK HOLDER)                       mockshop02@oksbi(MOCK SHOP)                  900000000001              PAY             111.11          DR       SUCCESS"
        val result = parser.parse(line)

        assertThat(result).hasSize(1)
        assertThat(result.first().parseError).isNotNull()
    }

    @Test
    fun `concurrent parsing yields correct dates`() {
        val line = "23/09/2025   01:38:31       State Bank Of India     XXXXXX0001                  mockholder01@upi(MOCK HOLDER)                       mockshop02@oksbi(MOCK SHOP)                  900000000001              PAY             111.11          DR       SUCCESS"
        val results = runBlocking {
            (1..20).map {
                async(Dispatchers.Default) {
                    parser.parse(line).first()
                }
            }.awaitAll()
        }

        assertThat(results).hasSize(20)
        results.forEach {
            assertThat(it.amount).isEqualTo(111.11)
            assertThat(it.parseError).isNull()
            val cal = java.util.Calendar.getInstance().apply { time = it.dateTime }
            assertThat(cal.get(java.util.Calendar.YEAR)).isEqualTo(2025)
            assertThat(cal.get(java.util.Calendar.MONTH)).isEqualTo(java.util.Calendar.SEPTEMBER)
            assertThat(cal.get(java.util.Calendar.DAY_OF_MONTH)).isEqualTo(23)
        }
    }
}
