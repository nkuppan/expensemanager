package com.naveenapps.expensemanager.core.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class ExpenseManagerScreens(
    val route: String,
    val navArguments: List<NamedNavArgument> = emptyList(),
) {
    val name: String = route.appendArguments(navArguments)

    data object Onboarding : ExpenseManagerScreens("onboarding")
    data object Home : ExpenseManagerScreens("home")
    data object AccountList : ExpenseManagerScreens(route = "account")
    data object CategoryList : ExpenseManagerScreens(route = "category")
    data object BudgetList : ExpenseManagerScreens(route = "budget")
    data object TransactionList : ExpenseManagerScreens(route = "transaction")
    data object Settings : ExpenseManagerScreens(route = "settings")
    data object CategoryTransaction : ExpenseManagerScreens(route = "categoryTransaction")
    data object AnalysisScreen : ExpenseManagerScreens(route = "analysis")
    data object ExportScreen : ExpenseManagerScreens(route = "export")
    data object ReminderScreen : ExpenseManagerScreens(route = "reminder")
    data object CurrencyCustomiseScreen : ExpenseManagerScreens(route = "currency_customise")
    data object AboutUsScreen : ExpenseManagerScreens(route = "about_us")

    data object AccountCreate : ExpenseManagerScreens(
        route = "account/create?accountId={accountId}",
        navArguments = listOf(
            navArgument("accountId") {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        const val KEY_ACCOUNT_ID = "accountId"

        fun createRoute(accountId: String) = name.replace("{${navArguments[0].name}}", accountId)
    }

    data object CategoryCreate : ExpenseManagerScreens(
        route = "category/create?categoryId={categoryId}",
        navArguments = listOf(
            navArgument("categoryId") {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        const val KEY_CATEGORY_ID = "categoryId"

        fun createRoute(categoryId: String) = name.replace("{${navArguments[0].name}}", categoryId)
    }

    data object CategoryDetails : ExpenseManagerScreens(
        route = "category/details?categoryId={categoryId}",
        navArguments = listOf(
            navArgument("categoryId") {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        const val KEY_CATEGORY_ID = "categoryId"

        fun createRoute(categoryId: String) = name.replace("{${navArguments[0].name}}", categoryId)
    }

    data object BudgetCreate : ExpenseManagerScreens(
        route = "budget/create?budgetId={budgetId}",
        navArguments = listOf(
            navArgument("budgetId") {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        const val KEY_BUDGET_ID = "budgetId"

        fun createRoute(budgetId: String) = name.replace("{${navArguments[0].name}}", budgetId)
    }

    data object BudgetDetails : ExpenseManagerScreens(
        route = "budget/details?budgetId={budgetId}",
        navArguments = listOf(
            navArgument("budgetId") {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        const val KEY_BUDGET_ID = "budgetId"

        fun createRoute(budgetId: String) = name.replace("{${navArguments[0].name}}", budgetId)
    }

    data object TransactionCreate : ExpenseManagerScreens(
        route = "transaction/create?transactionId={transactionId}",
        navArguments = listOf(
            navArgument("transactionId") {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        const val KEY_TRANSACTION_ID = "transactionId"

        fun createRoute(transactionId: String) =
            name.replace("{${navArguments[0].name}}", transactionId)
    }
}

private fun String.appendArguments(navArguments: List<NamedNavArgument>): String {
    val mandatoryArguments = navArguments.filter { it.argument.defaultValue == null }
        .takeIf { it.isNotEmpty() }
        ?.joinToString(separator = "/", prefix = "/") { "{${it.name}}" }
        .orEmpty()
    val optionalArguments = navArguments.filter { it.argument.defaultValue != null }
        .takeIf { it.isNotEmpty() }
        ?.joinToString(separator = "&", prefix = "?") { "${it.name}={${it.name}}" }
        .orEmpty()
    return "$this$mandatoryArguments$optionalArguments"
}
