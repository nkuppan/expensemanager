package com.naveenapps.expensemanager.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.naveenapps.expensemanager.feature.account.create.AccountCreateScreen
import com.naveenapps.expensemanager.feature.account.list.AccountListScreen
import com.naveenapps.expensemanager.feature.analysis.AnalysisScreen
import com.naveenapps.expensemanager.feature.budget.create.BudgetCreateScreen
import com.naveenapps.expensemanager.feature.budget.list.BudgetListScreen
import com.naveenapps.expensemanager.feature.category.create.CategoryCreateScreen
import com.naveenapps.expensemanager.feature.category.list.CategoryListScreen
import com.naveenapps.expensemanager.feature.category.transaction.CategoryTransactionTabScreen
import com.naveenapps.expensemanager.feature.dashboard.DashboardScreen
import com.naveenapps.expensemanager.feature.export.ExportScreen
import com.naveenapps.expensemanager.feature.onboarding.OnboardingScreen
import com.naveenapps.expensemanager.feature.settings.SettingsScreen
import com.naveenapps.expensemanager.feature.transaction.create.TransactionCreateScreen
import com.naveenapps.expensemanager.feature.transaction.list.TransactionListScreen


@Composable
fun HomePageNavHostContainer() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "onboarding"
    ) {
        composable("onboarding") {
            OnboardingScreen(navController)
        }
        composable("home") {
            HomeScreen(navController)
        }
        composable("category") {
            CategoryListScreen(navController)
        }
        composable(
            route = "category/create?categoryId={categoryId}",
            arguments = listOf(
                navArgument("categoryId") {
                    defaultValue = ""
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            CategoryCreateScreen(
                navController,
                backStackEntry.arguments?.getString("categoryId")
            )
        }
        composable("transaction") {
            TransactionListScreen(
                navController
            )
        }
        composable(
            route = "transaction/create?transactionId={transactionId}",
            arguments = listOf(
                navArgument("transactionId") {
                    defaultValue = ""
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            TransactionCreateScreen(
                navController,
                backStackEntry.arguments?.getString("transactionId")
            )
        }
        composable("account") {
            AccountListScreen(navController)
        }
        composable(
            route = "account/create?accountId={accountId}",
            arguments = listOf(
                navArgument("accountId") {
                    defaultValue = ""
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            AccountCreateScreen(
                navController,
                backStackEntry.arguments?.getString("accountId")
            )
        }
        composable("budget") {
            BudgetListScreen(navController)
        }
        composable(
            route = "budget/create?budgetId={budgetId}",
            arguments = listOf(
                navArgument("budgetId") {
                    defaultValue = ""
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            BudgetCreateScreen(
                navController,
                backStackEntry.arguments?.getString("budgetId")
            )
        }
        composable("analysis") {
            AnalysisScreen(navController)
        }
        composable("settings") {
            SettingsScreen(navController)
        }
        composable("export") {
            ExportScreen(navController)
        }
        composable("category_group") {
            CategoryTransactionTabScreen(navController)
        }
    }
}

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {

    Scaffold(
        bottomBar = {
            BottomAppBar {
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
                    AnalysisScreen(navController)
                }

                HomeScreenBottomBarItems.Transaction -> {
                    TransactionListScreen(navController)
                }

                HomeScreenBottomBarItems.Category -> {
                    CategoryTransactionTabScreen(navController)
                }
            }
        }
    }
}