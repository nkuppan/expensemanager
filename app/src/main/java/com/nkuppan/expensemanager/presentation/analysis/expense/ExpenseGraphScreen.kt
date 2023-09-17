package com.nkuppan.expensemanager.presentation.analysis.expense

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.nkuppan.expensemanager.core.ui.theme.ExpenseManagerTheme
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.entryModelOf

@Composable
fun ExpenseGraphScreen() {

    val viewModel: ExpenseGraphViewModel = hiltViewModel()

    val graphData by viewModel.graphItems.collectAsState()

    ChartScreen()
}

@Composable
private fun ChartScreen() {

    val chartEntryModel = entryModelOf(10, 20, 23, 17, 50)

    Chart(
        chart = columnChart(),
        model = chartEntryModel,
        startAxis = rememberStartAxis(
            title = "Transaction Amount"
        ),
        bottomAxis = rememberBottomAxis(
            title = "Days"
        ),
        isZoomEnabled = false
    )
}

@Preview
@Composable
fun ChartScreenPreview() {
    ExpenseManagerTheme {
        ChartScreen()
    }
}