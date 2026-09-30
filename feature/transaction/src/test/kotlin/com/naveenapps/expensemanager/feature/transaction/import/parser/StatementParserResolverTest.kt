package com.naveenapps.expensemanager.feature.transaction.import.parser

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StatementParserResolverTest {

    private val bhimText = """
        Transaction History
        Customer Mobile Number: +91XXXXXXXXX  Transaction History from 24/06/2025 to 24/09/2025
        Date       Time             Bank Name           Account Number      Sender      Receiver      Payment ID/Reference Number   Pay/Collect   Amount (in Rs.)    DR/CR        Status
        23/09/2025   01:38:31       State Bank Of India     XXXXXX0001                  mockholder01@upi(MOCK HOLDER)                       mockshop02@oksbi(MOCK SHOP)                  900000000001              PAY             111.11          DR       SUCCESS
    """.trimIndent()

    private val yonoText = """
        STATEMENT OF ACCOUNT
        State Bank of India
        WDL TFR
        01/06/2025   01/06/2025   UPI/DR/800000000001/ALPHA MART         -       111.11         -      12,34,567.89
    """.trimIndent()

    private val resolver = StatementParserResolver()

    @Test
    fun `selects the bhim parser for a transaction history`() {
        assertThat(resolver.select(bhimText)).isInstanceOf(BhimStatementParser::class.java)
    }

    @Test
    fun `selects the yono parser for an account statement`() {
        assertThat(resolver.select(yonoText)).isInstanceOf(SbiYonoStatementParser::class.java)
    }

    @Test
    fun `returns null for an unrecognised statement`() {
        assertThat(resolver.select("total 12 apples")).isNull()
    }

    @Test
    fun `built in parsers parse the statements they claim`() {
        val bhim = resolver.select(bhimText)?.parse(bhimText).orEmpty()
        val yono = resolver.select(yonoText)?.parse(yonoText).orEmpty()

        assertThat(bhim).hasSize(1)
        assertThat(bhim.first().amount).isEqualTo(111.11)
        assertThat(yono).hasSize(1)
        assertThat(yono.first().amount).isEqualTo(111.11)
    }

    @Test
    fun `reports the supported formats`() {
        assertThat(resolver.supportedFormats)
            .containsExactly("BHIM transaction history", "SBI YONO account statement").inOrder()
    }
}
