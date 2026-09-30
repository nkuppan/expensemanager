package com.naveenapps.expensemanager.feature.transaction.import.parser

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class StatementPdfTextExtractorTest {

    @Test
    fun `bounded writer accepts text within limit`() {
        val writer = StatementPdfTextExtractor.BoundedWriter(100)
        writer.write("hello world".toCharArray(), 0, 11)

        assertThat(writer.result()).isEqualTo("hello world")
    }

    @Test
    fun `bounded writer rejects text exceeding limit without full allocation`() {
        val writer = StatementPdfTextExtractor.BoundedWriter(10)
        writer.write("12345".toCharArray(), 0, 5)

        assertThrows(StatementPdfTextExtractor.TextTooLargeException::class.java) {
            writer.write("678901".toCharArray(), 0, 6)
        }
        assertThat(writer.result().length).isAtMost(10)
    }

    @Test
    fun `limits are positive and sane`() {
        assertThat(StatementPdfTextExtractor.MAX_PDF_BYTES).isGreaterThan(0L)
        assertThat(StatementPdfTextExtractor.MAX_PDF_PAGES).isGreaterThan(0)
        assertThat(StatementPdfTextExtractor.MAX_TEXT_CHARS).isGreaterThan(0)
    }
}
