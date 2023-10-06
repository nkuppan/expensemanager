package com.nkuppan.expensemanager.presentation.analysis

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.common.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.domain.model.UiState
import com.nkuppan.expensemanager.presentation.transaction.list.TransactionItem
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
fun AnalysisGraphScreen(navController: NavController) {

    val context = LocalContext.current

    val viewModel: AnalysisScreenViewModel = hiltViewModel()

    val graphData by viewModel.graphItems.collectAsState()

    when (val response = graphData) {
        UiState.Empty -> {

        }

        UiState.Loading -> {

        }

        is UiState.Success -> {
            val newGraphData = response.data

            LazyColumn {
                item {
                    Text(
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                        text = stringResource(id = R.string.month_analysis),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    ChartScreen(newGraphData.chartData)
                }

                items(newGraphData.transactions) {
                    TransactionItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("transaction/create?transactionId=${it.id}")
                            }
                            .padding(16.dp),
                        categoryName = it.categoryName,
                        categoryColor = it.categoryBackgroundColor,
                        categoryIcon = it.categoryIcon,
                        fromAccountName = it.fromAccountName,
                        fromAccountIcon = it.fromAccountIcon,
                        fromAccountColor = it.fromAccountColor,
                        amount = it.amount.asString(context),
                        date = it.date,
                        notes = it.notes,
                        transactionType = it.transactionType
                    )
                }
            }
        }
    }
}

@Composable
fun ChartScreen(
    chart: AnalysisChartData?,
    modifier: Modifier = Modifier,
) {

    chart ?: return

    val expenseColor = colorResource(id = R.color.red_500)
    val incomeColor = colorResource(id = R.color.green_500)

    val marker = rememberMarker()

    Chart(
        modifier = modifier,
        chart = lineChart(
            persistentMarkers = remember(marker) {
                mapOf(10f to marker)
            },
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
            itemPlacer = AxisItemPlacer.Vertical.default(4),
            label = axisLabelComponent(),
            valueFormatter = { value, chartValues ->
                String.format("%.2f", value)
            }
        ),
        bottomAxis = rememberBottomAxis(
            itemPlacer = AxisItemPlacer.Horizontal.default(6),
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
            AnalysisChartData(
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