package com.naveenapps.expensemanager.feature.analysis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.common.utils.getCompactNumber
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getExpenseColor
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getIncomeColor
import com.naveenapps.expensemanager.core.designsystem.utils.shouldUseDarkTheme
import com.naveenapps.expensemanager.core.model.AverageData
import com.naveenapps.expensemanager.core.model.ExpenseFlowState
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
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AnalysisGraphScreen(
    viewModel: AnalysisScreenViewModel = koinViewModel(),
) {
    val graphData by viewModel.graphItems.collectAsStateWithLifecycle()
    val averageData by viewModel.averageData.collectAsStateWithLifecycle()
    val amountUiState by viewModel.amountUiState.collectAsStateWithLifecycle()
    val transactionPeriod by viewModel.transactionPeriod.collectAsStateWithLifecycle()
    val insights by viewModel.insights.collectAsStateWithLifecycle()
    val comparison by viewModel.comparison.collectAsStateWithLifecycle()

    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val isDarkTheme = shouldUseDarkTheme(theme = currentTheme.mode)

    ChartScreenContent(
        graphData = graphData,
        isDarkTheme = isDarkTheme,
        amountUiState = amountUiState,
        transactionPeriod = transactionPeriod,
        averageData = averageData,
        insights = insights,
        comparison = comparison,
        onTransactionClick = viewModel::openTransaction,
        onCategoryClick = viewModel::openCategory,
        onViewAllCategories = viewModel::openCategoryList,
    )
}

@Composable
private fun ChartScreenContent(
    graphData: AnalysisUiData?,
    isDarkTheme: Boolean,
    amountUiState: ExpenseFlowState,
    transactionPeriod: String,
    averageData: WholeAverageData,
    insights: AnalysisInsightsUi? = null,
    comparison: PeriodComparisonUi? = null,
    onTransactionClick: (String) -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onViewAllCategories: () -> Unit = {},
) {
    // One clear order: the overview first, then where the money goes, how it changed, when it
    // was spent and the averages, ending with the biggest single expenses.
    Column(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OverviewCard(
            flow = amountUiState,
            comparison = comparison,
            chart = graphData?.chartData,
            isDarkTheme = isDarkTheme,
            period = transactionPeriod,
        )

        val shownInsights = insights?.takeIf { it.hasExpenses }
        shownInsights?.let { HighlightsCard(it) }
        shownInsights?.let {
            CategoryBreakdownSection(it.categories, onCategoryClick, onViewAllCategories)
        }

        comparison?.takeIf { it.categoryChanges.isNotEmpty() }?.let {
            PeriodComparisonCard(comparison = it, onCategoryClick = onCategoryClick)
        }

        shownInsights?.let { WeekdayCard(it.weekdays, it.busiestWeekday) }

        AveragesCard(averageData = averageData)

        shownInsights?.takeIf { it.biggestExpenses.isNotEmpty() }?.let {
            BiggestExpensesSection(it.biggestExpenses, onTransactionClick)
        }
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

@AppPreviewsLightAndDarkMode
@Composable
fun AmountSummaryPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        ChartScreenContent(
            isDarkTheme = true,
            graphData = AnalysisUiData(
                transactions = emptyList(),
                chartData = getChartData(),
            ),
            amountUiState = ExpenseFlowState(
                income = "$15000.0",
                expense = "$5000.0",
                balance = "$10000.0",
            ),
            transactionPeriod = "This month (Feb 2026)",
            averageData = WholeAverageData(
                expenseAverageData = AverageData(
                    perDay = "10.0$",
                    perWeek = "10.0$",
                    perMonth = "10.0$",
                ),
                incomeAverageData = AverageData(
                    perDay = "10.0$",
                    perWeek = "10.0$",
                    perMonth = "10.0$",
                ),
            ),
        )
    }
}

@Composable
private fun getChartData(): AnalysisUiChartData = AnalysisUiChartData(
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
)
