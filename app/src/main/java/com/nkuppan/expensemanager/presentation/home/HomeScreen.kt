package com.nkuppan.expensemanager.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nkuppan.expensemanager.presentation.analysis.AnalysisScreen
import com.nkuppan.expensemanager.presentation.category.list.CategoryListScreen
import com.nkuppan.expensemanager.presentation.dashboard.DashboardScreen
import com.nkuppan.expensemanager.presentation.dashboard.DashboardViewModel
import com.nkuppan.expensemanager.presentation.transaction.list.TransactionListScreen

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {

    Scaffold(
        bottomBar = {
            NavigationBar {
                HomeScreenBottomBarItems.values().forEach { uiSystem ->
                    NavigationBarItem(
                        selected = viewModel.homeScreenBottomBarItems == uiSystem,
                        onClick = { viewModel.setUISystem(uiSystem) },
                        icon = {
                            Icon(
                                painterResource(uiSystem.iconResourceID),
                                stringResource(uiSystem.labelResourceID),
                            )
                        },
                        label = { Text(stringResource(uiSystem.labelResourceID)) },
                    )
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(
                bottom = paddingValues.calculateBottomPadding()
            ),
        ) {
            when (viewModel.homeScreenBottomBarItems) {
                HomeScreenBottomBarItems.Home -> {
                    DashboardScreen(navController)
                }

                HomeScreenBottomBarItems.Analysis -> {
                    AnalysisScreen(navController = navController)
                }

                HomeScreenBottomBarItems.Transaction -> {
                    TransactionListScreen(navController = navController)
                }

                HomeScreenBottomBarItems.Category -> {
                    CategoryListScreen(navController = navController)
                }
            }
        }
    }
}