package com.naveenapps.expensemanager.presentation.analysis

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBar
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme

@Composable
fun AnalysisScreen(navController: NavController) {
    AnalysisScreenScaffoldView(navController = navController)
}


@Composable
private fun AnalysisScreenScaffoldView(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopNavigationBar(
                navController = navController,
                title = stringResource(R.string.analysis),
                disableBackIcon = true
            )
        }
    ) { innerPadding ->
        AnalysisScreenContent(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            navController = navController
        )
    }
}

@Composable
private fun AnalysisScreenContent(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        AnalysisGraphScreen()
    }
}

@Preview
@Composable
fun AnalysisScreenPreview() {
    ExpenseManagerTheme {
        AnalysisScreen(rememberNavController())
    }
}