package com.naveenapps.expensemanager.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.ActivityComponentProvider
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

@Composable
fun MainScreen(
    composeNavigator: AppComposeNavigator,
    componentProvider: ActivityComponentProvider,
    isDarkTheme: Boolean,
    landingScreen: ExpenseManagerScreens,
    pendingQuickAdd: Boolean = false,
    onQuickAddHandled: () -> Unit = {},
) {
    ExpenseManagerTheme(isDarkTheme = isDarkTheme) {
        val navHostController = rememberNavController()
        val analyticsRepository: AnalyticsRepository = koinInject()

        // One screen_view per navigation destination. Type-safe routes look like
        // "com.naveenapps...ExpenseManagerScreens.TransactionCreate?id={id}", so keep only
        // the class name ("TransactionCreate") to get stable, readable screen names.
        DisposableEffect(navHostController) {
            val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
                val screenName = destination.route
                    ?.substringBefore('?')
                    ?.substringBefore('/')
                    ?.substringAfterLast('.')
                    ?: return@OnDestinationChangedListener
                analyticsRepository.setCurrentScreen(screenName)
            }
            navHostController.addOnDestinationChangedListener(listener)
            onDispose { navHostController.removeOnDestinationChangedListener(listener) }
        }

        LaunchedEffect(Unit) {
            composeNavigator.handleNavigationCommands(navHostController)
        }

        // Quick add (widget "+", launcher shortcut, reminder tap): open the keypad on top of
        // Home. Navigate the controller directly rather than via composeNavigator, whose
        // command stream drops anything sent before its collector subscribes (cold start).
        LaunchedEffect(pendingQuickAdd) {
            if (!pendingQuickAdd) return@LaunchedEffect
            // Wait until the NavHost has its graph and a start destination.
            snapshotFlow { navHostController.currentBackStackEntry }.filterNotNull().first()
            navHostController.navigate(ExpenseManagerScreens.TransactionCreate(null)) {
                launchSingleTop = true
            }
            onQuickAddHandled()
        }

        HomePageNavHostContainer(
            componentProvider,
            navHostController,
            landingScreen,
        )
    }
}
