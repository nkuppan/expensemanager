package com.nkuppan.expensemanager.presentation.analysis

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.core.ui.theme.NavigationButton
import com.nkuppan.expensemanager.presentation.analysis.expense.AnalysisGraphScreen

@Composable
fun AnalysisScreen(navController: NavController) {
    AnalysisScreenScaffoldView(navController = navController)
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AnalysisScreenScaffoldView(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    NavigationButton(navController)
                },
                title = {
                    Text(text = stringResource(R.string.analysis))
                }
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
        AnalysisGraphScreen(navController)
    }
}

@Preview
@Composable
fun AnalysisScreenPreview() {
    ExpenseManagerTheme {
        AnalysisScreen(rememberNavController())
    }
}