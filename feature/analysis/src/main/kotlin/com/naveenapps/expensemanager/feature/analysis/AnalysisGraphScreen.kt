package com.naveenapps.expensemanager.feature.analysis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
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

// The x-axis labels (one date per point) travel with the chart's data.
private val DatesKey = ExtraStore.Key<List<String>>()

private const val START_AXIS_LABEL_COUNT = 6
private const val BOTTOM_AXIS_LABEL_SPACING = 5
private const val AREA_FILL_TOP_ALPHA = 0.4f

/** Expense and income per day of the selected period, as two lines (Vico 3). */
@Composable
fun ChartScreen(
    chart: AnalysisUiChartData?,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    if (chart == null || chart.expenses.isEmpty()) return

    val expenseColor = getExpenseColor()
    val incomeColor = getIncomeColor()

    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(chart) {
        modelProducer.runTransaction {
            lineModel {
                series(x = chart.expenses.indices.toList(), y = chart.expenses)
                series(x = chart.incomes.indices.toList(), y = chart.incomes)
            }
            extras { it[DatesKey] = chart.dates }
        }
    }

    // Axis labels, guidelines and the marker follow the app's Material theme (light or dark,
    // whichever the user picked in Settings, not only the system setting).
    key(isDarkTheme) {
        ProvideVicoTheme(rememberM3VicoTheme()) {
            CartesianChartHost(
                modifier = modifier,
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(
                            rememberChartLine(expenseColor),
                            rememberChartLine(incomeColor),
                        ),
                    ),
                    startAxis = VerticalAxis.rememberStart(
                        valueFormatter = remember {
                            CartesianValueFormatter { _, value, _ -> getCompactNumber(value) }
                        },
                        itemPlacer = remember { VerticalAxis.ItemPlacer.count({ START_AXIS_LABEL_COUNT }) },
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = remember {
                            CartesianValueFormatter { context, x, _ ->
                                context.model.extraStore.getOrNull(DatesKey)?.getOrNull(x.toInt()).orEmpty()
                            }
                        },
                        itemPlacer = remember { HorizontalAxis.ItemPlacer.aligned({ BOTTOM_AXIS_LABEL_SPACING }) },
                    ),
                    marker = rememberDefaultCartesianMarker(
                        label = rememberTextComponent(
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                        ),
                        guideline = rememberAxisGuidelineComponent(),
                    ),
                ),
                modelProducer = modelProducer,
            )
        }
    }
}

/** A smooth line in [color] with a fading area under it. */
@Composable
private fun rememberChartLine(color: Color): LineCartesianLayer.Line = LineCartesianLayer.rememberLine(
    fill = remember(color) { LineCartesianLayer.LineFill.single(Fill(color)) },
    areaFill = remember(color) {
        LineCartesianLayer.AreaFill.single(
            Fill(Brush.verticalGradient(listOf(color.copy(alpha = AREA_FILL_TOP_ALPHA), Color.Transparent))),
        )
    },
    interpolator = remember { LineCartesianLayer.Interpolator.cubic() },
)

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

private fun getChartData(): AnalysisUiChartData = AnalysisUiChartData(
    expenses = listOf(1.0, 2.0, 3.0, 3.0, 2.5, 4.0, 3.0, 2.0, 3.5, 3.0),
    incomes = listOf(4.0, 3.0, 2.0, 3.0, 3.0, 3.0, 5.0, 3.0, 3.0, 3.0),
    dates = listOf("08/09", "18/09", "21/09", "24/09", "28/09", "01/10", "04/10", "08/10", "12/10", "16/10"),
)
