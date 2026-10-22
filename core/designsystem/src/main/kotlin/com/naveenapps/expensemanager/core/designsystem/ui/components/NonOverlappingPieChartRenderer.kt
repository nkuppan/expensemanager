package com.naveenapps.expensemanager.core.designsystem.ui.components

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.renderer.PieChartRenderer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Outside labels for the category donut: a two-line block (the category name in the theme's
 * text colour over its share in the slice's colour), joined to its slice by a thin leader line
 * with a dot at each end.
 *
 * MPAndroidChart draws each OUTSIDE_SLICE label at its slice's own angle with no collision
 * handling. This renderer guarantees instead that:
 * - labels never overlap each other;
 * - leader lines never cross the ring (so never the centre text) and never cross each other.
 *
 * How: each label goes on the side its slice points to, and the labels of a side keep the
 * slices' order while being spread apart vertically. A leader goes straight from just outside
 * its slice to its label, so same-order endpoints give non-crossing lines. Any label whose line
 * would still hit the ring or another line, or that doesn't fit, is left off, smallest slice
 * first; a left-off label of a slice near the top or bottom then gets a try on the other side,
 * kept only if the whole chart still passes. The category list under the chart names every
 * category. Names too long for the
 * space beside the chart are ellipsized instead of being clipped.
 */
internal class NonOverlappingPieChartRenderer(
    chart: PieChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler,
) : PieChartRenderer(chart, animator, viewPortHandler) {

    /** Colour of the category names; set from the Compose theme so it follows dark mode. */
    var labelTextColor: Int = Color.DKGRAY

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val namePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }
    private val percentPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.DEFAULT_BOLD
    }
    private val linePath = Path()

    private class Label(
        val name: String,
        val share: Float,
        val color: Int,
        /** Horizontal direction of the slice's middle (cosine of its angle). */
        val dx: Float,
        var isRight: Boolean,
        /** Dot on the ring, at the middle of the slice. */
        val startX: Float,
        val startY: Float,
        /** Just outside the ring, straight out from [startX], [startY]. */
        val elbowX: Float,
        val elbowY: Float,
    ) {
        /** Vertical centre of the label (and the end of its leader line). */
        var y: Float = elbowY
    }

    override fun drawValues(c: Canvas) {
        val pieData = mChart.data ?: return
        val dataSet = pieData.dataSet as? PieDataSet ?: return
        if (!dataSet.isDrawValuesEnabled || dataSet.entryCount == 0) return
        if (dataSet.xValuePosition != PieDataSet.ValuePosition.OUTSIDE_SLICE) {
            super.drawValues(c)
            return
        }

        val center = mChart.centerCircleBox
        val radius = mChart.radius
        val rotation = mChart.rotationAngle
        val drawAngles = mChart.drawAngles
        val absoluteAngles = mChart.absoluteAngles
        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY
        // PieData (not the data set) exposes the total in MPAndroidChart 3.1.
        val total = pieData.yValueSum

        namePaint.textSize = dataSet.valueTextSize
        namePaint.color = labelTextColor
        percentPaint.textSize = dataSet.valueTextSize
        val nameMetrics = namePaint.fontMetrics
        val percentMetrics = percentPaint.fontMetrics
        val blockHeight = (nameMetrics.descent - nameMetrics.ascent) + (percentMetrics.descent - percentMetrics.ascent)
        val minGap = blockHeight + Utils.convertDpToPixel(LABEL_SPACING_DP)

        val ringGap = Utils.convertDpToPixel(RING_GAP_DP)
        val elbowRadius = radius + Utils.convertDpToPixel(ELBOW_OFFSET_DP)
        val ringClearance = radius + ringGap
        val textPadding = Utils.convertDpToPixel(TEXT_PADDING_DP)
        val edgePadding = Utils.convertDpToPixel(EDGE_PADDING_DP)
        val startDotRadius = Utils.convertDpToPixel(START_DOT_DP)
        val endDotRadius = Utils.convertDpToPixel(END_DOT_DP)
        // Every label of a side lines up in one column beyond the elbows.
        val columnOffset = elbowRadius + Utils.convertDpToPixel(TAIL_LENGTH_DP)

        val candidates = ArrayList<Label>(dataSet.entryCount)
        for (i in 0 until dataSet.entryCount) {
            val entry = dataSet.getEntryForIndex(i)
            if (entry.y <= 0f || i >= drawAngles.size) continue
            val name = dataSet.valueFormatter.getPieLabel(entry.y, entry)
            if (name.isNullOrEmpty()) continue

            val start = if (i == 0) 0f else absoluteAngles[i - 1] * phaseX
            val rad = Math.toRadians((rotation + (start + drawAngles[i] / 2f) * phaseY).toDouble())
            val dx = cos(rad).toFloat()
            val dy = sin(rad).toFloat()
            candidates += Label(
                name = name,
                share = if (total > 0f) entry.y / total * 100f else 0f,
                color = dataSet.getColor(i),
                dx = dx,
                isRight = dx >= 0f,
                startX = center.x + dx * (radius + ringGap),
                startY = center.y + dy * (radius + ringGap),
                elbowX = center.x + dx * elbowRadius,
                elbowY = center.y + dy * elbowRadius,
            )
        }

        val top = blockHeight / 2f + edgePadding
        val bottom = c.height - blockHeight / 2f - edgePadding
        val perSide = floor((bottom - top) / minGap).toInt() + 1

        fun columnX(isRight: Boolean) = center.x + (if (isRight) 1f else -1f) * columnOffset
        fun place(isRight: Boolean, labels: List<Label>): List<Label> = placeSide(
            labels = labels,
            columnX = columnX(isRight),
            minGap = minGap,
            top = top,
            bottom = bottom,
            isClear = { label ->
                distanceToSegment(center.x, center.y, label.elbowX, label.elbowY, columnX(isRight), label.y) >= ringClearance
            },
        )

        val sides = mutableMapOf(
            true to place(true, candidates.filter { it.isRight }.sortedByDescending { it.share }.take(perSide)),
            false to place(false, candidates.filter { !it.isRight }.sortedByDescending { it.share }.take(perSide)),
        )

        // A label left off on a crowded side may fit on the other one if its slice is near the
        // top or bottom. Accept the move only if every rule still holds for the whole chart.
        val dropped = candidates.filter { it !in sides.getValue(true) && it !in sides.getValue(false) }
        for (label in dropped.sortedByDescending { it.share }) {
            if (abs(label.dx) >= SWITCHABLE_DX) continue
            val target = !label.isRight
            val current = sides.getValue(target)
            if (current.size >= perSide) continue
            label.isRight = target
            val trial = place(target, current + label)
            if (trial.size == current.size + 1 && !anyCrossing(sides.getValue(!target) + trial, ::columnX)) {
                sides[target] = trial
            } else {
                label.isRight = !target
                sides[target] = place(target, current) // restore positions
            }
        }
        val placed = sides.getValue(true) + sides.getValue(false)

        linePaint.strokeWidth = Utils.convertDpToPixel(LINE_WIDTH_DP)
        for (label in placed) {
            val side = if (label.isRight) 1f else -1f
            val endX = center.x + side * columnOffset

            // Leader line: out from the slice, then straight to the label.
            val lineColor = withAlpha(label.color, LINE_ALPHA)
            linePaint.color = lineColor
            linePath.reset()
            linePath.moveTo(label.startX, label.startY)
            linePath.lineTo(label.elbowX, label.elbowY)
            linePath.lineTo(endX, label.y)
            c.drawPath(linePath, linePaint)

            dotPaint.color = lineColor
            c.drawCircle(label.startX, label.startY, startDotRadius, dotPaint)
            dotPaint.color = label.color
            c.drawCircle(endX, label.y, endDotRadius, dotPaint)

            // Two lines centred on the end of the leader: the name, then the share.
            val textStart = endX + side * (endDotRadius + textPadding)
            val available = if (label.isRight) c.width - edgePadding - textStart else textStart - edgePadding
            val name = TextUtils.ellipsize(label.name, namePaint, available.coerceAtLeast(0f), TextUtils.TruncateAt.END).toString()
            val percent = formatPercent(label.share)
            percentPaint.color = label.color

            val nameBaseline = label.y - blockHeight / 2f - nameMetrics.ascent
            val percentBaseline = nameBaseline + nameMetrics.descent - percentMetrics.ascent
            if (label.isRight) {
                c.drawText(name, textStart, nameBaseline, namePaint)
                c.drawText(percent, textStart, percentBaseline, percentPaint)
            } else {
                c.drawText(name, textStart - namePaint.measureText(name), nameBaseline, namePaint)
                c.drawText(percent, textStart - percentPaint.measureText(percent), percentBaseline, percentPaint)
            }
        }
    }

    /**
     * Places one side's labels in slice order, [minGap] apart, as close as possible to their
     * slices. While any leader line would hit the ring ([isClear]) or cross a neighbour's, the
     * smallest slice's label is dropped and the rest are placed again.
     */
    private fun placeSide(
        labels: List<Label>,
        columnX: Float,
        minGap: Float,
        top: Float,
        bottom: Float,
        isClear: (Label) -> Boolean,
    ): List<Label> {
        val remaining = labels.sortedBy { it.elbowY }.toMutableList()
        while (remaining.isNotEmpty()) {
            spreadVertically(remaining, minGap, top, bottom)
            val crossing = remaining.zipWithNext().any { (a, b) -> segmentsIntersect(a, b, columnX) }
            if (remaining.all(isClear) && !crossing) return remaining
            remaining.remove(remaining.minBy { it.share })
        }
        return remaining
    }

    /** Keeps [sorted] in order while guaranteeing [minGap] between neighbours, within bounds. */
    private fun spreadVertically(sorted: List<Label>, minGap: Float, top: Float, bottom: Float) {
        sorted.forEach { it.y = it.elbowY }
        // Push down from the top…
        var previous = top - minGap
        for (label in sorted) {
            label.y = max(label.y, previous + minGap)
            previous = label.y
        }
        // …then pull back up from the bottom if we ran past it.
        var next = bottom + minGap
        for (label in sorted.asReversed()) {
            label.y = min(label.y, next - minGap)
            next = label.y
        }
    }

    /** Whether the elbow→label segments of two labels on the same side cross. */
    private fun segmentsIntersect(a: Label, b: Label, columnX: Float): Boolean = segmentsCross(a.elbowX, a.elbowY, columnX, a.y, b.elbowX, b.elbowY, columnX, b.y)

    /** Whether any two labels' leader lines (stub and leader) cross anywhere on the chart. */
    private fun anyCrossing(labels: List<Label>, columnX: (Boolean) -> Float): Boolean {
        fun segments(l: Label) = listOf(
            floatArrayOf(l.startX, l.startY, l.elbowX, l.elbowY),
            floatArrayOf(l.elbowX, l.elbowY, columnX(l.isRight), l.y),
        )
        for (i in labels.indices) {
            for (j in i + 1 until labels.size) {
                for (a in segments(labels[i])) {
                    for (b in segments(labels[j])) {
                        if (segmentsCross(a[0], a[1], a[2], a[3], b[0], b[1], b[2], b[3])) return true
                    }
                }
            }
        }
        return false
    }

    private companion object {
        const val LINE_WIDTH_DP = 1.2f
        const val LINE_ALPHA = 0.75f
        const val RING_GAP_DP = 3f
        const val ELBOW_OFFSET_DP = 10f
        const val TAIL_LENGTH_DP = 10f
        const val TEXT_PADDING_DP = 5f
        const val EDGE_PADDING_DP = 4f
        const val LABEL_SPACING_DP = 6f
        const val START_DOT_DP = 1.6f
        const val END_DOT_DP = 2.4f

        /** |cos| of a slice's middle angle below which its label may move to the other side. */
        const val SWITCHABLE_DX = 0.55f

        /** Whether segments (ax1, ay1)–(ax2, ay2) and (bx1, by1)–(bx2, by2) properly cross. */
        @Suppress("LongParameterList")
        fun segmentsCross(
            ax1: Float,
            ay1: Float,
            ax2: Float,
            ay2: Float,
            bx1: Float,
            by1: Float,
            bx2: Float,
            by2: Float,
        ): Boolean {
            fun cross(ox: Float, oy: Float, px: Float, py: Float, qx: Float, qy: Float) = (px - ox) * (qy - oy) - (py - oy) * (qx - ox)
            val d1 = cross(bx1, by1, bx2, by2, ax1, ay1)
            val d2 = cross(bx1, by1, bx2, by2, ax2, ay2)
            val d3 = cross(ax1, ay1, ax2, ay2, bx1, by1)
            val d4 = cross(ax1, ay1, ax2, ay2, bx2, by2)
            return ((d1 > 0f && d2 < 0f) || (d1 < 0f && d2 > 0f)) && ((d3 > 0f && d4 < 0f) || (d3 < 0f && d4 > 0f))
        }

        /** Shortest distance from point (px, py) to the segment (ax, ay)–(bx, by). */
        fun distanceToSegment(px: Float, py: Float, ax: Float, ay: Float, bx: Float, by: Float): Float {
            val vx = bx - ax
            val vy = by - ay
            val lengthSquared = vx * vx + vy * vy
            val t = if (lengthSquared == 0f) 0f else (((px - ax) * vx + (py - ay) * vy) / lengthSquared).coerceIn(0f, 1f)
            val dx = px - (ax + t * vx)
            val dy = py - (ay + t * vy)
            return sqrt(dx * dx + dy * dy)
        }

        /** "42%", or "<1%" so a sliver never reads as zero. */
        fun formatPercent(percent: Float): String = if (percent < 0.5f) "<1%" else "${percent.roundToInt()}%"

        fun withAlpha(color: Int, alpha: Float): Int = Color.argb((Color.alpha(color) * alpha).roundToInt(), Color.red(color), Color.green(color), Color.blue(color))
    }
}
