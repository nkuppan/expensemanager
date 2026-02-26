package com.naveenapps.expensemanager.feature.analysis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.common.utils.getCompactNumber
import com.naveenapps.expensemanager.core.designsystem.components.AmountInfoWidget
import com.naveenapps.expensemanager.core.designsystem.components.DashboardWidgetTitle
import com.naveenapps.expensemanager.core.designsystem.components.EmptyItem
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
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
    viewModel: AnalysisScreenViewModel = koinViewModel()
) {

    val graphData by viewModel.graphItems.collectAsState()
    val averageData by viewModel.averageData.collectAsState()
    val amountUiState by viewModel.amountUiState.collectAsState()
    val transactionPeriod by viewModel.transactionPeriod.collectAsState()

    val currentTheme by viewModel.currentTheme.collectAsState()
    val isDarkTheme = shouldUseDarkTheme(theme = currentTheme.mode)

    ChartScreenContent(
        graphData = graphData,
        isDarkTheme = isDarkTheme,
        amountUiState = amountUiState,
        transactionPeriod = transactionPeriod,
        averageData = averageData
    )
}

@Composable
private fun ChartScreenContent(
    graphData: AnalysisUiData?,
    isDarkTheme: Boolean,
    amountUiState: ExpenseFlowState,
    transactionPeriod: String,
    averageData: WholeAverageData
) {
    Column(
        modifier = Modifier
            .padding(top = 16.dp, bottom = 16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        graphData?.chartData?.let {
            AppCardView(
                Modifier.padding(horizontal = 16.dp),
            ) {
                ChartScreen(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    chart = it,
                    isDarkTheme = isDarkTheme,
                )
            }
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
                .padding(horizontal = 16.dp),
        )

        TransactionAverageItem(
            modifier = Modifier.padding(horizontal = 16.dp),
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
    val incomeColor = colorResource(
        id = com.naveenapps.expensemanager.core.common.R.color.green_500,
    )
    val expenseColor = colorResource(
        id = com.naveenapps.expensemanager.core.common.R.color.red_500,
    )
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)

    AppCardView(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            DashboardWidgetTitle(
                title = stringResource(id = R.string.average_and_projected),
            )

            Spacer(Modifier.height(16.dp))

            // ── Column headers ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Period column — same weight as data rows
                Spacer(Modifier.weight(1f))

                Text(
                    text = stringResource(id = com.naveenapps.expensemanager.core.designsystem.R.string.income),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                    ),
                    color = incomeColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(id = com.naveenapps.expensemanager.core.designsystem.R.string.expense),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                    ),
                    color = expenseColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(10.dp))

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
                    .copy(alpha = 0.35f),
            )

            Spacer(Modifier.height(10.dp))

            // ── Data rows ──────────────────────────────────────────
            AverageRow(
                label = stringResource(id = R.string.day),
                income = averageData.incomeAverageData.perDay,
                expense = averageData.expenseAverageData.perDay,
                incomeColor = incomeColor,
                expenseColor = expenseColor,
                labelColor = labelColor,
            )

            Spacer(Modifier.height(12.dp))

            AverageRow(
                label = stringResource(id = R.string.week),
                income = averageData.incomeAverageData.perWeek,
                expense = averageData.expenseAverageData.perWeek,
                incomeColor = incomeColor,
                expenseColor = expenseColor,
                labelColor = labelColor,
            )

            Spacer(Modifier.height(12.dp))

            AverageRow(
                label = stringResource(id = R.string.month),
                income = averageData.incomeAverageData.perMonth,
                expense = averageData.expenseAverageData.perMonth,
                incomeColor = incomeColor,
                expenseColor = expenseColor,
                labelColor = labelColor,
            )
        }
    }
}

@Composable
private fun AverageRow(
    label: String,
    income: String,
    expense: String,
    incomeColor: Color,
    expenseColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
            ),
            color = labelColor,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = income,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.3).sp,
            ),
            color = incomeColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = expense,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.3).sp,
            ),
            color = expenseColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
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

@AppPreviewsLightAndDarkMode
@Composable
fun AmountSummaryPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
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
