package com.naveenapps.expensemanager.core.domain.usecase.receipt

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** One line of recognised text and where it sits in the image (pixels; angle in degrees). */
data class RecognisedLine(
    val text: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val angle: Float = 0f,
)

/**
 * Puts recognised text back into the receipt's printed rows. Text recognition returns a bill's
 * columns as separate blocks ("Grand Total" in one, "106.05" in another), so read in order the
 * label and its amount never meet. Lines whose vertical centres line up are joined left to right
 * instead, after undoing the photo's tilt (the median angle of the lines).
 */
object ReceiptRows {

    fun build(lines: List<RecognisedLine>): String {
        val usable = lines.filter { it.text.isNotBlank() && it.bottom > it.top }
        if (usable.isEmpty()) return ""
        val radians = Math.toRadians(-usable.map { it.angle.toDouble() }.sorted()[usable.size / 2])
        val placed = usable.map { line ->
            val cx = (line.left + line.right) / 2.0
            val cy = (line.top + line.bottom) / 2.0
            Placed(
                text = line.text.trim(),
                x = cx * cos(radians) - cy * sin(radians),
                y = cx * sin(radians) + cy * cos(radians),
                height = (line.bottom - line.top).toDouble(),
            )
        }.sortedBy { it.y }

        val rows = mutableListOf<MutableList<Placed>>()
        placed.forEach { line ->
            val row = rows.lastOrNull()
            if (row != null && line.belongsTo(row)) row += line else rows += mutableListOf(line)
        }
        return rows.joinToString("\n") { row -> row.sortedBy { it.x }.joinToString("  ") { it.text } }
    }

    private class Placed(val text: String, val x: Double, val y: Double, val height: Double)

    // Same row: the centres are within about half a line's height of each other.
    private fun Placed.belongsTo(row: List<Placed>): Boolean {
        val rowY = row.sumOf { it.y } / row.size
        val rowHeight = row.sumOf { it.height } / row.size
        return abs(y - rowY) <= SAME_ROW * minOf(height, rowHeight)
    }

    private const val SAME_ROW = 0.6
}
