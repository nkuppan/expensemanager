package com.naveenapps.expensemanager.core.designsystem.ui.components

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.renderer.PieChartRenderer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * MPAndroidChart draws each OUTSIDE_SLICE label at its slice's own angle with no collision
 * handling, so neighbouring thin slices produce stacked, unreadable labels. This renderer
 * lays the labels out per side (left / right of the donut), pushes them apart vertically so
 * they never overlap, and bends the leader line to follow each label.
 */
internal class NonOverlappingPieChartRenderer(
    chart: PieChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler,
) : PieChartRenderer(chart, animator, viewPortHandler) {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val linePath = Path()

    private class Label(
        val text: String,
        val color: Int,
        val startX: Float,
        val startY: Float,
        val elbowX: Float,
        val desiredY: Float,
        val isRight: Boolean,
    ) {
        var y: Float = desiredY
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

        mValuePaint.textSize = dataSet.valueTextSize
        val fm = mValuePaint.fontMetrics
        val textHeight = fm.descent - fm.ascent
        val minGap = textHeight + Utils.convertDpToPixel(2f)

        val elbowOffset = Utils.convertDpToPixel(10f)
        val tailLength = Utils.convertDpToPixel(14f)
        val textPadding = Utils.convertDpToPixel(4f)

        val labels = ArrayList<Label>(dataSet.entryCount)
        for (i in 0 until dataSet.entryCount) {
            val entry = dataSet.getEntryForIndex(i)
            if (entry.y <= 0f || i >= drawAngles.size) continue
            val text = dataSet.valueFormatter.getPieLabel(entry.y, entry)
            if (text.isNullOrEmpty()) continue

            val start = if (i == 0) 0f else absoluteAngles[i - 1] * phaseX
            val midDeg = rotation + (start + drawAngles[i] / 2f) * phaseY
            val rad = Math.toRadians(midDeg.toDouble())
            val dx = cos(rad).toFloat()
            val dy = sin(rad).toFloat()

            labels += Label(
                text = text,
                color = dataSet.getColor(i),
                startX = center.x + dx * radius,
                startY = center.y + dy * radius,
                elbowX = center.x + dx * (radius + elbowOffset),
                desiredY = center.y + dy * (radius + elbowOffset),
                isRight = dx >= 0f,
            )
        }

        val top = textHeight / 2f + textPadding
        val bottom = c.height - textHeight / 2f - textPadding
        spreadVertically(labels.filter { it.isRight }, minGap, top, bottom)
        spreadVertically(labels.filter { !it.isRight }, minGap, top, bottom)

        linePaint.strokeWidth = dataSet.valueLineWidth
        val baselineShift = -(fm.ascent + fm.descent) / 2f

        for (label in labels) {
            val endX = if (label.isRight) {
                max(label.elbowX, center.x + radius + elbowOffset) + tailLength
            } else {
                min(label.elbowX, center.x - radius - elbowOffset) - tailLength
            }

            linePaint.color = if (dataSet.isUsingSliceColorAsValueLineColor) label.color else dataSet.valueLineColor
            linePath.reset()
            linePath.moveTo(label.startX, label.startY)
            linePath.lineTo(label.elbowX, label.y)
            linePath.lineTo(endX, label.y)
            c.drawPath(linePath, linePaint)

            mValuePaint.color = label.color
            mValuePaint.textAlign = if (label.isRight) Paint.Align.LEFT else Paint.Align.RIGHT
            val textX = if (label.isRight) endX + textPadding else endX - textPadding
            c.drawText(label.text, textX, label.y + baselineShift, mValuePaint)
        }
        mValuePaint.textAlign = Paint.Align.CENTER
    }

    /** Keeps labels in their natural order while guaranteeing [minGap] between neighbours. */
    private fun spreadVertically(side: List<Label>, minGap: Float, top: Float, bottom: Float) {
        if (side.isEmpty()) return
        val sorted = side.sortedBy { it.desiredY }
        // If they can't all fit, squeeze the gap so everything stays on-canvas.
        val gap = if (sorted.size > 1) min(minGap, (bottom - top) / (sorted.size - 1)) else minGap

        // Push down from the top…
        var previous = top - gap
        for (label in sorted) {
            label.y = max(label.desiredY, previous + gap)
            previous = label.y
        }
        // …then pull back up from the bottom if we ran past it.
        var next = bottom + gap
        for (label in sorted.asReversed()) {
            label.y = min(label.y, next - gap)
            next = label.y
        }
    }
}
