package com.naveenapps.expensemanager.presentation.analysis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.core.common.utils.UiState
import com.naveenapps.expensemanager.core.model.AverageData
import com.naveenapps.expensemanager.core.model.WholeAverageData
import com.naveenapps.expensemanager.presentation.dashboard.DashboardWidgetTitle
import com.naveenapps.expensemanager.presentation.dashboard.FilterView
import com.naveenapps.expensemanager.presentation.dashboard.IncomeExpenseBalanceView
import com.naveenapps.expensemanager.ui.theme.ExpenseManagerTheme
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf

@Composable
fun AnalysisGraphScreen() {

    val viewModel: AnalysisScreenViewModel = hiltViewModel()

    val graphData by viewModel.graphItems.collectAsState()
    val averageData by viewModel.averageData.collectAsState()
    val amountUiState by viewModel.amountUiState.collectAsState()

    when (val response = graphData) {
        UiState.Empty -> {

        }

        UiState.Loading -> {

        }

        is UiState.Success -> {
            val newGraphData = response.data

            LazyColumn {
                item {
                    FilterView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 6.dp)
                    )
                }
                item {
                    ChartScreen(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                        chart = newGraphData.chartData
                    )
                }

                item {
                    IncomeExpenseBalanceView(
                        amountUiState = amountUiState,
                        showBalance = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    )
                }

                item {
                    TransactionAverageItem(
                        modifier = Modifier.padding(16.dp),
                        averageData = averageData
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionAverageItem(
    averageData: WholeAverageData,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            DashboardWidgetTitle(title = stringResource(id = com.naveenapps.expensemanager.core.data.R.string.average))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = stringResource(id = com.naveenapps.expensemanager.core.data.R.string.day))
                    Text(text = stringResource(id = com.naveenapps.expensemanager.core.data.R.string.week))
                    Text(text = stringResource(id = com.naveenapps.expensemanager.core.data.R.string.month))
                }
                AverageAmountItems(
                    averageData = averageData.incomeAverageData,
                    textColor = colorResource(id = R.color.green_500)
                )
                AverageAmountItems(
                    averageData = averageData.expenseAverageData,
                    textColor = colorResource(id = R.color.red_500)
                )
            }
        }
    }
}

@Composable
private fun AverageAmountItems(
    averageData: AverageData,
    textColor: Color = colorResource(id = R.color.green_500)
) {
    Column(
        modifier = Modifier.wrapContentSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.End),
            text = averageData.perDay,
            color = textColor,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.End
        )
        Text(
            modifier = Modifier.align(Alignment.End),
            text = averageData.perWeek,
            color = textColor,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.End
        )
        Text(
            modifier = Modifier.align(Alignment.End),
            text = averageData.perMonth,
            color = textColor,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun ChartScreen(
    chart: AnalysisUiChartData?,
    modifier: Modifier = Modifier,
) {

    chart ?: return

    val expenseColor = colorResource(id = R.color.red_500)
    val incomeColor = colorResource(id = R.color.green_500)

    val marker = rememberMarker()

    Chart(
        modifier = modifier,
        chart = lineChart(
            lines = listOf(
                lineSpec(
                    lineColor = expenseColor,
                    lineBackgroundShader = verticalGradient(
                        arrayOf(
                            expenseColor.copy(0.5f),
                            expenseColor.copy(alpha = 0f)
                        ),
                    ),
                ),
                lineSpec(
                    lineColor = incomeColor,
                    lineBackgroundShader = verticalGradient(
                        arrayOf(
                            incomeColor.copy(0.5f),
                            incomeColor.copy(alpha = 0f)
                        ),
                    ),
                )
            ),
        ),
        startAxis = rememberStartAxis(
            itemPlacer = AxisItemPlacer.Vertical.default(6),
            label = axisLabelComponent(),
            valueFormatter = { value, _ ->
                String.format("%.1f", value)
            }
        ),
        bottomAxis = rememberBottomAxis(
            itemPlacer = AxisItemPlacer.Horizontal.default(5),
            label = axisLabelComponent(),
            valueFormatter = { value, _ ->
                if (chart.dates.isNotEmpty()) {
                    chart.dates[value.toInt()]
                } else {
                    ""
                }
            }
        ),
        model = chart.chartData,
        marker = marker,
    )
}

@Preview
@Composable
fun ChartScreenPreview() {
    ExpenseManagerTheme {
        ChartScreen(
            AnalysisUiChartData(
                chartData = entryModelOf(
                    listOf(
                        entryOf(0, 1),
                        entryOf(1, 2),
                        entryOf(2, 3),
                        entryOf(2, 3),
                        entryOf(2, 3),
                        entryOf(2, 3),
                        entryOf(2, 3),
                        entryOf(2, 3),
                        entryOf(2, 3),
                        entryOf(2, 3),
                        entryOf(2, 3)
                    ),
                    listOf(
                        entryOf(0, 4),
                        entryOf(1, 3),
                        entryOf(2, 2),
                        entryOf(2, 3),
                        entryOf(2, 3),
                        entryOf(2, 3),
                        entryOf(2, 3),
                        entryOf(2, 3),
                        entryOf(2, 3),
                        entryOf(2, 3),
                        entryOf(2, 3)
                    ),
                ),
                dates = listOf(
                    "08/09",
                    "18/09",
                    "21/09",
                    "24/09",
                    "28/09",
                    "18/09",
                    "21/09",
                    "24/09",
                    "28/09",
                    "28/09"
                )
            )
        )
    }
}

@com.naveenapps.expensemanager.core.designsystem.AppPreviewsLightAndDarkMode
@Composable
fun TransactionAverageItemPreview() {
    ExpenseManagerTheme {
        TransactionAverageItem(
            WholeAverageData(
                AverageData(
                    "10.0$",
                    "10.0$",
                    "10.0$",
                ),
                AverageData(
                    "10.0$",
                    "10.0$",
                    "10.0$",
                )
            ),
        )
    }
}