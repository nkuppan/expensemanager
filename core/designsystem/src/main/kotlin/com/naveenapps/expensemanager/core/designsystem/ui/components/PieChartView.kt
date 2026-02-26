package com.naveenapps.expensemanager.core.designsystem.ui.components

import android.widget.LinearLayout
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter

data class PieChartUiData(
    val name: String,
    val value: Float,
    val color: Int,
)

@Composable
fun PieChartView(
    totalAmountText: String,
    chartData: List<PieChartUiData>,
    modifier: Modifier = Modifier,
    chartHeight: Int = 600,
    hideValues: Boolean = false,
    chartWidth: Int = LinearLayout.LayoutParams.MATCH_PARENT,
) {
    var isAnimated by remember { mutableStateOf(false) }

    val onBackgroundColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val holeColor = MaterialTheme.colorScheme.background.toArgb()
    val surfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
        .copy(alpha = 0.5f).toArgb()

    Crossfade(targetState = chartData, label = "pie_chart") { pieChartData ->
        AndroidView(
            modifier = modifier.wrapContentSize(),
            factory = { context ->
                PieChart(context).apply {
                    layoutParams = LinearLayout.LayoutParams(chartWidth, chartHeight)

                    // ── Disable chrome ──────────────────────────────
                    description.isEnabled = false
                    legend.isEnabled = false
                    isHighlightPerTapEnabled = false
                    isDragDecelerationEnabled = false
                    setTouchEnabled(false)

                    // ── Donut hole ──────────────────────────────────
                    // Slightly smaller hole radius + a translucent outer
                    // ring creates a softer, more modern donut look.
                    isDrawHoleEnabled = true
                    holeRadius = 76f
                    transparentCircleRadius = 80f
                    setTransparentCircleAlpha(40)
                    setTransparentCircleColor(holeColor)
                    setHoleColor(holeColor)
                    setDrawSlicesUnderHole(false)

                    // ── Centre text ─────────────────────────────────
                    // Bold typeface + slightly smaller size for a cleaner
                    // look than the default regular-weight text.
                    setDrawCenterText(true)
                    setCenterTextColor(onBackgroundColor)
                    setCenterTextSize(if (hideValues) 12f else 15f)
                    setCenterTextTypeface(android.graphics.Typeface.DEFAULT_BOLD)
                    centerText = totalAmountText

                    // ── Entry labels (on-slice text) off ────────────
                    // Removes the cramped text drawn directly on thin
                    // slices — outer value lines handle labelling.
                    setDrawEntryLabels(false)

                    // ── Percent mode ────────────────────────────────
                    setUsePercentValues(true)

                    // ── Start at 12 o'clock ─────────────────────────
                    rotationAngle = 270f
                    isRotationEnabled = false

                    // ── Extra offsets for outer label breathing room ─
                    if (!hideValues) {
                        setExtraOffsets(24f, 12f, 24f, 12f)
                    } else {
                        setExtraOffsets(8f, 8f, 8f, 8f)
                    }

                    // ── Animate only on first composition ───────────
                    if (!isAnimated) {
                        isAnimated = true
                        animateY(900, Easing.EaseInOutCubic)
                    }
                }
            },
            update = { chart ->
                // Re-apply theme colours on recomposition (e.g. dark mode toggle)
                chart.setCenterTextColor(onBackgroundColor)
                chart.setHoleColor(holeColor)
                chart.setTransparentCircleColor(holeColor)
                chart.centerText = totalAmountText
                updatePieChartWithData(chart, pieChartData, hideValues, surfaceVariantColor)
            },
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  Data binding
// ═══════════════════════════════════════════════════════════════════════

private fun updatePieChartWithData(
    chart: PieChart,
    data: List<PieChartUiData>,
    hideValues: Boolean,
    labelFallbackColor: Int,
) {
    val entries = data.map { PieEntry(it.value, "", it.name) }
    val colors = data.map { it.color }

    val dataSet = PieDataSet(entries, "").apply {
        isHighlightEnabled = false
        this.colors = colors

        // ── Slice gap ──────────────────────────────────────────
        // Slightly wider than default for cleaner separation.
        sliceSpace = if (data.size > 1) 3f else 0f
        selectionShift = 0f

        // ── Value lines + outer labels ─────────────────────────
        if (!hideValues) {
            setDrawValues(true)

            // Connecting lines from slice to label
            valueLinePart1Length = 0.3f
            valueLinePart1OffsetPercentage = 85f
            valueLinePart2Length = 0.4f
            valueLineWidth = 1.5f
            valueLineColor = labelFallbackColor
            isUsingSliceColorAsValueLineColor = true

            // Labels sit outside the donut
            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE

            valueTextSize = 11f
            setValueTextColors(colors)

            // Show category name instead of raw value
            valueFormatter = object : ValueFormatter() {
                override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
                    return pieEntry?.data?.toString() ?: ""
                }
            }
        } else {
            setDrawValues(false)
        }
    }

    chart.data = PieData(dataSet)
    chart.invalidate()
}
