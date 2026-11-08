package com.naveenapps.expensemanager.core.domain.usecase.receipt

import com.google.common.truth.Truth.assertThat
import kotlin.math.cos
import kotlin.math.sin
import org.junit.Test

class ReceiptRowsTest {

    // A two-column bill as text recognition returns it: the labels block first, then the amounts.
    private val labels = listOf(
        RecognisedLine("FRESH MART", left = 100, top = 0, right = 400, bottom = 40),
        RecognisedLine("Bread", left = 20, top = 100, right = 120, bottom = 130),
        RecognisedLine("Sub Total", left = 20, top = 140, right = 180, bottom = 170),
        RecognisedLine("Grand Total", left = 20, top = 180, right = 210, bottom = 212),
    )
    private val amounts = listOf(
        RecognisedLine("45.00", left = 400, top = 102, right = 480, bottom = 131),
        RecognisedLine("101.00", left = 390, top = 141, right = 480, bottom = 169),
        RecognisedLine("106.05", left = 390, top = 183, right = 480, bottom = 214),
    )

    @Test
    fun `labels and amounts in separate blocks are joined into their rows`() {
        val text = ReceiptRows.build(labels + amounts)

        assertThat(text.lines()).containsExactly(
            "FRESH MART",
            "Bread  45.00",
            "Sub Total  101.00",
            "Grand Total  106.05",
        ).inOrder()
        assertThat(ReceiptParser.findBillTotal(text.lines())).isEqualTo(106.05)
    }

    @Test
    fun `a tilted photo still keeps each row together`() {
        val degrees = 4f
        val radians = Math.toRadians(degrees.toDouble())
        // Rotate every line about the origin as a slightly tilted photo would.
        val tilted = (labels + amounts).map { line ->
            val cx = (line.left + line.right) / 2.0
            val cy = (line.top + line.bottom) / 2.0
            val x = cx * cos(radians) - cy * sin(radians)
            val y = cx * sin(radians) + cy * cos(radians)
            val halfWidth = (line.right - line.left) / 2
            val halfHeight = (line.bottom - line.top) / 2
            line.copy(
                left = (x - halfWidth).toInt(),
                right = (x + halfWidth).toInt(),
                top = (y - halfHeight).toInt(),
                bottom = (y + halfHeight).toInt(),
                angle = degrees,
            )
        }

        assertThat(ReceiptRows.build(tilted).lines().last()).isEqualTo("Grand Total  106.05")
    }

    @Test
    fun `no lines gives no text`() {
        assertThat(ReceiptRows.build(emptyList())).isEmpty()
    }
}
