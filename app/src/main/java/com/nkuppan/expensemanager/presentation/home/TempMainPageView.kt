package com.nkuppan.expensemanager.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nkuppan.expensemanager.core.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.presentation.account.create.AccountCreateScreen
import com.nkuppan.expensemanager.presentation.account.list.AccountListScreen
import com.nkuppan.expensemanager.presentation.analysis.AnalysisScreen
import com.nkuppan.expensemanager.presentation.budget.create.BudgetCreateScreen
import com.nkuppan.expensemanager.presentation.budget.list.BudgetListScreen
import com.nkuppan.expensemanager.presentation.category.create.CategoryCreateScreen
import com.nkuppan.expensemanager.presentation.category.list.CategoryListScreen
import com.nkuppan.expensemanager.presentation.settings.SettingsScreen
import com.nkuppan.expensemanager.presentation.transaction.create.TransactionCreateScreen
import com.nkuppan.expensemanager.presentation.transaction.list.TransactionListScreen


@Composable
fun TempMainPageView() {

    val navController = rememberNavController()

    Scaffold {
        it.calculateTopPadding()
        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                TempHomePageScreen(navController)
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
                TransactionListScreen(navController)
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
            composable("new_home") {
                HomeScreen(navController)
            }
        }
    }
}

@Composable
private fun TempHomePageScreen(navController: NavHostController) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Center)
        ) {
            Button(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally),
                onClick = {
                    navController.navigate("category")
                }) {
                Text(text = "Navigate to Category")
            }
            Button(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally), onClick = {
                    navController.navigate("category/create")
                }) {
                Text(text = "Navigate to Category Create")
            }
            Button(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally), onClick = {
                    navController.navigate("transaction")
                }) {
                Text(text = "Navigate to Transaction")
            }
            Button(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally), onClick = {
                    navController.navigate("transaction/create")
                }) {
                Text(text = "Navigate to Transaction Create")
            }
            Button(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally), onClick = {
                    navController.navigate("analysis")
                }) {
                Text(text = "Navigate to Analysis")
            }
            Button(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally),
                onClick = {
                    navController.navigate("account")
                }) {
                Text(text = "Navigate to Account List Screen")
            }
            Button(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally),
                onClick = {
                    navController.navigate("account/create")
                }) {
                Text(text = "Navigate to Account Create Screen")
            }
            Button(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally),
                onClick = {
                    navController.navigate("budget")
                }) {
                Text(text = "Navigate to Budget List Screen")
            }
            Button(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally),
                onClick = {
                    navController.navigate("budget/create")
                }) {
                Text(text = "Navigate to Budget Create Screen")
            }
            Button(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally),
                onClick = {
                    navController.navigate("settings")
                }) {
                Text(text = "Navigate to Settings")
            }
            Button(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally),
                onClick = {
                    navController.navigate("new_home")
                }) {
                Text(text = "Navigate New Home Page")
            }
        }
    }
}

@Preview
@Composable
fun MainPagePreview() {

    ExpenseManagerTheme {
        TempMainPageView()
    }
}