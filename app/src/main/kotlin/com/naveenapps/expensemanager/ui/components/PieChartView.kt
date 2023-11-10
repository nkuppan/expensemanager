package com.naveenapps.expensemanager.ui.components

import android.widget.LinearLayout
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.buildSpannedString
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.naveenapps.expensemanager.core.model.PieChartData


@Composable
fun PieChartView(
    totalAmountText: String,
    chartData: List<PieChartData>,
    modifier: Modifier = Modifier,
    chartHeight: Int = 600,
    hideValues: Boolean = false,
    chartWidth: Int = LinearLayout.LayoutParams.MATCH_PARENT
) {
    val colorCode = MaterialTheme.colorScheme.onBackground.hashCode()
    val holeColor = MaterialTheme.colorScheme.background.hashCode()

    Crossfade(targetState = chartData, label = "") { pieChartData ->
        // on below line we are creating an
        // android view for pie chart.
        AndroidView(
            modifier = modifier.wrapContentSize(),
            factory = { context ->
                // on below line we are creating a pie chart
                // and specifying layout params.
                PieChart(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        // on below line we are specifying layout
                        // params as MATCH PARENT for height and width.
                        chartWidth,
                        chartHeight,
                    )
                    // on below line we are setting description
                    // enables for our pie chart.
                    this.description.isEnabled = false

                    // on below line we are setting draw hole
                    // to false not to draw hole in pie chart
                    this.isHighlightPerTapEnabled = false
                    this.isDragDecelerationEnabled = false
                    this.isDrawHoleEnabled = true
                    this.holeRadius = 75f
                    this.setHoleColor(holeColor)
                    this.setTouchEnabled(false)
                    this.setUsePercentValues(true)
                    this.setDrawSlicesUnderHole(true)
                    if (hideValues) {
                        this.setCenterTextSize(12f)
                    } else {
                        this.setCenterTextSize(16f)
                    }

                    this.setCenterTextColor(colorCode)
                    this.centerText = buildSpannedString {
                        append(totalAmountText)
                    }

                    // on below line we are enabling legend.
                    this.legend.isEnabled = false
                    this.animateY(1000, Easing.EaseInOutQuad)
                }
            },
            update = {
                // on below line we are calling update pie chart
                // method and passing pie chart and list of data.
                updatePieChartWithData(it, pieChartData, hideValues)
            }
        )
    }
}

fun updatePieChartWithData(
    chart: PieChart,
    data: List<PieChartData>,
    hideValues: Boolean
) {
    val entries = mutableListOf<PieEntry>()
    val colors = mutableListOf<Int>()

    for (i in data.indices) {
        val item = data[i]
        entries.add(
            PieEntry(item.value)
        )
        colors.add(item.color)
    }

    val pieDataSet = PieDataSet(entries, "")

    pieDataSet.isHighlightEnabled = false
    pieDataSet.colors = colors
    pieDataSet.setValueTextColors(colors)
    pieDataSet.sliceSpace = 3f
    if (hideValues) {
        pieDataSet.valueTextSize = 6f
    } else {
        pieDataSet.valueTextSize = 10f
    }
    pieDataSet.isUsingSliceColorAsValueLineColor = true
    pieDataSet.valueLinePart1Length = .2f
    pieDataSet.valueLineWidth = 2f

    if (hideValues.not()) {
        pieDataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        pieDataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        pieDataSet.valueFormatter = object : ValueFormatter() {
            override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
                return data.find { it.value == pieEntry?.value }?.name ?: ""
            }
        }
    } else {
        pieDataSet.valueFormatter = object : ValueFormatter() {
            override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
                return ""
            }
        }
    }

    val pieData = PieData(pieDataSet)
    chart.data = pieData
    chart.invalidate()
}