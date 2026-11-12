package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.PieChartMeasuringContext
import com.patrykandpatrick.vico.compose.pie.PieSize
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.PieValueFormatter
import com.patrykandpatrick.vico.compose.pie.data.pieModel
import com.patrykandpatrick.vico.compose.pie.rememberPieChart

data class PieChartUiData(
    val name: String,
    val value: Float,
    val color: Int,
)

private const val HOLE_RATIO = 0.76f
private val SliceSpacing = 2.dp
private const val LINE_ALPHA = 0.8f

/**
 * A donut of [chartData] (Vico pie chart) with [totalAmountText] in the hole. Unless
 * [hideValues], each slice's name sits outside it on a line in the slice's colour.
 */
@Composable
fun PieChartView(
    totalAmountText: String,
    chartData: List<PieChartUiData>,
    modifier: Modifier = Modifier,
    chartAreaHeight: Dp = 200.dp,
    hideValues: Boolean = false,
) {
    // Vico needs at least one value; slices of 0 draw nothing anyway.
    val slices = chartData.filter { it.value > 0f }.withDonutHoleFix()

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        if (slices.isNotEmpty()) {
            DonutChart(slices, chartAreaHeight, hideValues)
        }
        Text(
            text = totalAmountText,
            style = if (hideValues) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DonutChart(
    slices: List<PieChartUiData>,
    chartAreaHeight: Dp,
    hideValues: Boolean,
) {
    val labelText = rememberTextComponent(
        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface),
    )
    val sliceProvider = remember(slices, hideValues, labelText) {
        val styles = slices.map { slice -> sliceStyle(slice, labelText.takeUnless { hideValues }) }
        PieChart.SliceProvider { _, index, _ -> styles[index % styles.size] }
    }
    // Outside labels show the category name rather than the amount.
    val names = remember(slices) { slices.map { it.name } }
    val valueFormatter = remember(names) {
        object : PieValueFormatter {
            override fun format(context: PieChartMeasuringContext, value: Float, index: Int): CharSequence = names.getOrElse(index) { "" }
        }
    }
    val chart = rememberPieChart(
        sliceProvider = sliceProvider,
        spacing = if (slices.count { it.name.isNotEmpty() } > 1) SliceSpacing else 0.dp,
        innerSize = PieSize.Inner.fixed(chartAreaHeight * HOLE_RATIO),
        valueFormatter = valueFormatter,
    )
    val modelProducer = remember { PieChartModelProducer() }
    LaunchedEffect(slices) {
        modelProducer.runTransaction {
            pieModel { series(slices.map { it.value }) }
        }
    }
    PieChartHost(
        chart = chart,
        modelProducer = modelProducer,
        chartAreaHeight = chartAreaHeight,
    )
}

// Vico draws a single value as a full pie, ignoring the hole (vico issue #1614). A second,
// invisible sliver keeps it a donut until that's fixed.
private const val SLIVER_FRACTION = 1e-4f

private fun List<PieChartUiData>.withDonutHoleFix(): List<PieChartUiData> = if (size == 1) {
    this + PieChartUiData(name = "", value = first().value * SLIVER_FRACTION, color = Color.Transparent.toArgb())
} else {
    this
}

private fun sliceStyle(slice: PieChartUiData, label: TextComponent?): PieChart.Slice {
    val color = Color(slice.color)
    if (slice.name.isEmpty()) return PieChart.Slice(fill = Fill(color))
    return PieChart.Slice(
        fill = Fill(color),
        label = label?.let {
            PieChart.SliceLabel.Outside(textComponent = it, lineColor = color.copy(alpha = LINE_ALPHA))
        },
    )
}
