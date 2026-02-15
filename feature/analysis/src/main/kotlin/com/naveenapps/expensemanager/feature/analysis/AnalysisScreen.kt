package com.naveenapps.expensemanager.feature.analysis

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.feature.filter.FilterView

@Composable
fun AnalysisScreen() {
    AnalysisScreenScaffoldView()
}

@Composable
private fun AnalysisScreenScaffoldView() {
    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                title = stringResource(R.string.analysis),
            )
        },
    ) { innerPadding ->
        AnalysisScreenContent(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        )
    }
}

@Composable
private fun AnalysisScreenContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        FilterView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 6.dp),
        )
        AnalysisGraphScreen()
    }
}

@Preview
@Composable
fun AnalysisScreenPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        AnalysisScreen()
    }
}
