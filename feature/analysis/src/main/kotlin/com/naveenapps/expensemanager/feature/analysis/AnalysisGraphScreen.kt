package com.naveenapps.expensemanager.feature.analysis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import com.naveenapps.expensemanager.core.common.utils.getCompactNumber
import com.naveenapps.expensemanager.core.designsystem.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.components.AMOUNT_VALUE
import com.naveenapps.expensemanager.core.designsystem.components.AmountInfoWidget
import com.naveenapps.expensemanager.core.designsystem.components.DashboardWidgetTitle
import com.naveenapps.expensemanager.core.designsystem.components.EmptyItem
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getExpenseColor
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getIncomeColor
import com.naveenapps.expensemanager.core.designsystem.utils.shouldUseDarkTheme
import com.naveenapps.expensemanager.core.model.ExpenseFlowState
import com.naveenapps.expensemanager.core.model.AverageData
import com.naveenapps.expensemanager.core.model.WholeAverageData
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf

@Composable
fun AnalysisGraphScreen(
    viewModel: AnalysisScreenViewModel = koinViewModel()
) {

    val graphData by viewModel.graphItems.collectAsState()
    val averageData by viewModel.averageData.collectAsState()
    val amountUiState by viewModel.amountUiState.collectAsState()
    val transactionPeriod by viewModel.transactionPeriod.collectAsState()

    val currentTheme by viewModel.currentTheme.collectAsState()
    val isDarkTheme = shouldUseDarkTheme(theme = currentTheme.mode)

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        graphData?.chartData?.let {
            ChartScreen(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                chart = it,
                isDarkTheme = isDarkTheme,
            )
        } ?: run {
            EmptyItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                emptyItemText = stringResource(id = R.string.no_chart_available),
                icon = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_analysis
            )
        }

        IncomeExpenseBalanceView(
            expenseFlowState = amountUiState,
            transactionPeriod = transactionPeriod,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        )

        TransactionAverageItem(
            modifier = Modifier.padding(16.dp),
            averageData = averageData,
        )
    }
}

@Composable
fun IncomeExpenseBalanceView(
    expenseFlowState: ExpenseFlowState,
    transactionPeriod: String,
    modifier: Modifier = Modifier,
) {
    AmountInfoWidget(
        expenseAmount = expenseFlowState.expense,
        incomeAmount = expenseFlowState.income,
        balanceAmount = expenseFlowState.balance,
        transactionPeriod = transactionPeriod,
        modifier = modifier,
    )
}

@Composable
fun TransactionAverageItem(
    averageData: WholeAverageData,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            DashboardWidgetTitle(title = stringResource(id = R.string.average_and_projected))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.day),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Normal,
                    )
                    Text(
                        text = stringResource(id = R.string.week),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Normal,
                    )
                    Text(
                        text = stringResource(id = R.string.month),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Normal,
                    )
                }
                AverageAmountItems(
                    averageData = averageData.incomeAverageData,
                    textColor = colorResource(id = com.naveenapps.expensemanager.core.common.R.color.green_500),
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
                AverageAmountItems(
                    averageData = averageData.expenseAverageData,
                    textColor = colorResource(id = com.naveenapps.expensemanager.core.common.R.color.red_500),
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.CenterVertically),
                )
            }
        }
    }
}

@Composable
private fun AverageAmountItems(
    averageData: AverageData,
    modifier: Modifier = Modifier,
    textColor: Color = colorResource(id = com.naveenapps.expensemanager.core.common.R.color.green_500),
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            modifier = Modifier.align(Alignment.End),
            text = averageData.perDay,
            color = textColor,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.End,
        )
        Text(
            modifier = Modifier.align(Alignment.End),
            text = averageData.perWeek,
            color = textColor,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.End,
        )
        Text(
            modifier = Modifier.align(Alignment.End),
            text = averageData.perMonth,
            color = textColor,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
fun ChartScreen(
    chart: AnalysisUiChartData?,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    chart ?: return

    val expenseColor = getExpenseColor()
    val incomeColor = getIncomeColor()

    val marker = rememberMarker()

    ProvideChartStyle(rememberChartStyle(chartColors, isDarkTheme)) {
        Chart(
            modifier = modifier,
            chart = lineChart(
                lines = listOf(
                    lineSpec(
                        lineColor = expenseColor,
                        lineBackgroundShader = verticalGradient(
                            arrayOf(
                                expenseColor.copy(0.5f),
                                expenseColor.copy(alpha = 0f),
                            ),
                        ),
                    ),
                    lineSpec(
                        lineColor = incomeColor,
                        lineBackgroundShader = verticalGradient(
                            arrayOf(
                                incomeColor.copy(0.5f),
                                incomeColor.copy(alpha = 0f),
                            ),
                        ),
                    ),
                ),
            ),
            startAxis = rememberStartAxis(
                itemPlacer = AxisItemPlacer.Vertical.default(6),
                label = axisLabelComponent(),
                valueFormatter = { value, _ ->
                    getCompactNumber(value)
                },
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
                },
            ),
            model = chart.chartData,
            marker = marker,
        )
    }
}

@Preview
@Composable
fun ChartScreenPreview() {
    ExpenseManagerTheme {
        ChartScreen(
            isDarkTheme = true,
            chart = AnalysisUiChartData(
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
                        entryOf(2, 3),
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
                        entryOf(2, 3),
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
                    "28/09",
                ),
            ),
        )
    }
}

@AppPreviewsLightAndDarkMode
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
                ),
            ),
        )
    }
}

@AppPreviewsLightAndDarkMode
@Composable
fun AmountSummaryPreview() {
    ExpenseManagerTheme {
        AmountInfoWidget(
            expenseAmount = AMOUNT_VALUE,
            incomeAmount = AMOUNT_VALUE,
            balanceAmount = AMOUNT_VALUE,
            transactionPeriod = "This Month(Oct 2023)",
        )
    }
}
