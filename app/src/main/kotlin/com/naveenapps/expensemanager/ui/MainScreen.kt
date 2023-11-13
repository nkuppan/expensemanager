package com.naveenapps.expensemanager.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator

@Composable
fun MainScreen(
    composeNavigator: AppComposeNavigator,
    isDarkTheme: Boolean,
    landingScreen: String,
) {
    ExpenseManagerTheme(isDarkTheme = isDarkTheme) {
        val navHostController = rememberNavController()

        LaunchedEffect(Unit) {
            composeNavigator.handleNavigationCommands(navHostController)
        }

        HomePageNavHostContainer(navHostController, landingScreen)
    }
}