package com.naveenapps.expensemanager.ui

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.naveenapps.expensemanager.core.designsystem.utils.BackHandler
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.feature.about.AboutScreen
import com.naveenapps.expensemanager.feature.account.create.AccountCreateScreen
import com.naveenapps.expensemanager.feature.account.list.AccountListScreen
import com.naveenapps.expensemanager.feature.analysis.AnalysisScreen
import com.naveenapps.expensemanager.feature.budget.create.BudgetCreateScreen
import com.naveenapps.expensemanager.feature.budget.details.BudgetDetailScreen
import com.naveenapps.expensemanager.feature.budget.list.BudgetListScreen
import com.naveenapps.expensemanager.feature.category.create.CategoryCreateScreen
import com.naveenapps.expensemanager.feature.category.details.CategoryDetailScreen
import com.naveenapps.expensemanager.feature.category.list.CategoryListScreen
import com.naveenapps.expensemanager.feature.category.transaction.CategoryTransactionTabScreen
import com.naveenapps.expensemanager.feature.currency.CurrencyCustomiseScreen
import com.naveenapps.expensemanager.feature.dashboard.DashboardScreen
import com.naveenapps.expensemanager.feature.export.ExportScreen
import com.naveenapps.expensemanager.feature.onboarding.OnboardingScreen
import com.naveenapps.expensemanager.feature.reminder.ReminderScreen
import com.naveenapps.expensemanager.feature.settings.SettingsScreen
import com.naveenapps.expensemanager.feature.transaction.create.TransactionCreateScreen
import com.naveenapps.expensemanager.feature.transaction.list.TransactionListScreen

@Composable
fun HomePageNavHostContainer(
    navHostController: NavHostController,
    landingScreen: String,
) {
    NavHost(
        navController = navHostController,
        startDestination = landingScreen,
    ) {
        this.expenseManagerNavigation()
    }
}

fun NavGraphBuilder.expenseManagerNavigation() {
    composable(route = ExpenseManagerScreens.Onboarding.route) {
        OnboardingScreen()
    }
    composable(ExpenseManagerScreens.Home.route) {
        HomeScreen()
    }
    composable(ExpenseManagerScreens.CategoryList.route) {
        CategoryListScreen()
    }
    composable(
        route = ExpenseManagerScreens.CategoryCreate.name,
        arguments = ExpenseManagerScreens.CategoryCreate.navArguments,
    ) {
        CategoryCreateScreen()
    }
    composable(
        route = ExpenseManagerScreens.CategoryDetails.name,
        arguments = ExpenseManagerScreens.CategoryDetails.navArguments,
    ) {
        CategoryDetailScreen()
    }
    composable(ExpenseManagerScreens.TransactionList.route) {
        TransactionListScreen()
    }
    composable(
        route = ExpenseManagerScreens.TransactionCreate.name,
        arguments = ExpenseManagerScreens.TransactionCreate.navArguments,
    ) {
        TransactionCreateScreen()
    }
    composable(ExpenseManagerScreens.AccountList.route) {
        AccountListScreen()
    }
    composable(
        route = ExpenseManagerScreens.AccountCreate.name,
        arguments = ExpenseManagerScreens.AccountCreate.navArguments,
    ) {
        AccountCreateScreen()
    }
    composable(ExpenseManagerScreens.BudgetList.route) {
        BudgetListScreen()
    }
    composable(
        route = ExpenseManagerScreens.BudgetCreate.name,
        arguments = ExpenseManagerScreens.BudgetCreate.navArguments,
    ) {
        BudgetCreateScreen()
    }
    composable(
        route = ExpenseManagerScreens.BudgetDetails.name,
        arguments = ExpenseManagerScreens.BudgetDetails.navArguments,
    ) {
        BudgetDetailScreen()
    }
    composable(ExpenseManagerScreens.AnalysisScreen.route) {
        AnalysisScreen()
    }
    composable(ExpenseManagerScreens.Settings.route) {
        SettingsScreen()
    }
    composable(ExpenseManagerScreens.ExportScreen.route) {
        ExportScreen()
    }
    composable(ExpenseManagerScreens.ReminderScreen.route) {
        ReminderScreen()
    }
    composable(ExpenseManagerScreens.CurrencyCustomiseScreen.route) {
        CurrencyCustomiseScreen()
    }
    composable(ExpenseManagerScreens.CategoryTransaction.route) {
        CategoryTransactionTabScreen()
    }
    composable(ExpenseManagerScreens.AboutUsScreen.route) {
        AboutScreen()
    }
    composable(ExpenseManagerScreens.AdvancedSettingsScreen.route) {
        AboutScreen()
    }
}

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val context = LocalContext.current

    BackHandler {
        if (viewModel.homeScreenBottomBarItems != HomeScreenBottomBarItems.Home) {
            viewModel.setUISystem(HomeScreenBottomBarItems.Home)
        } else {
            (context as? Activity)?.finish()
        }
    }

    Scaffold(
        bottomBar = {
            BottomAppBar {
                HomeScreenBottomBarItems.entries.forEach { uiSystem ->
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
                bottom = paddingValues.calculateBottomPadding(),
            ),
        ) {
            when (viewModel.homeScreenBottomBarItems) {
                HomeScreenBottomBarItems.Home -> {
                    DashboardScreen()
                }

                HomeScreenBottomBarItems.Analysis -> {
                    AnalysisScreen()
                }

                HomeScreenBottomBarItems.Transaction -> {
                    TransactionListScreen()
                }

                HomeScreenBottomBarItems.Category -> {
                    CategoryTransactionTabScreen()
                }
            }
        }
    }
}
