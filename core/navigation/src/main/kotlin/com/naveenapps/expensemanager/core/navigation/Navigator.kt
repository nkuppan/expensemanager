package com.naveenapps.expensemanager.core.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onSubscription

abstract class Navigator {
    val navigationCommands =
        MutableSharedFlow<NavigationCommand>(extraBufferCapacity = Int.MAX_VALUE)

    val navControllerFlow = MutableStateFlow<NavController?>(null)

    fun navigateUp() {
        navigationCommands.tryEmit(NavigationCommand.NavigateUp)
    }
}

abstract class AppComposeNavigator : Navigator() {
    abstract fun navigate(route: String, optionsBuilder: (NavOptionsBuilder.() -> Unit)? = null)
    abstract fun <T> navigateBackWithResult(key: String, result: T, route: String?)

    abstract fun popUpTo(route: String, inclusive: Boolean)
    abstract fun navigateAndClearBackStack(route: String)

    abstract fun popBackStack()

    suspend fun handleNavigationCommands(navController: NavController) {
        navigationCommands
            .onSubscription { this@AppComposeNavigator.navControllerFlow.value = navController }
            .onCompletion { this@AppComposeNavigator.navControllerFlow.value = null }
            .collect { navController.handleComposeNavigationCommand(it) }
    }

    private fun NavController.handleComposeNavigationCommand(navigationCommand: NavigationCommand) {
        when (navigationCommand) {
            is ComposeNavigationCommand.NavigateToRoute -> {
                navigate(navigationCommand.route, navigationCommand.options)
            }

            NavigationCommand.NavigateUp -> navigateUp()
            is ComposeNavigationCommand.PopUpToRoute -> popBackStack(
                navigationCommand.route,
                navigationCommand.inclusive
            )

            is ComposeNavigationCommand.NavigateUpWithResult<*> -> {
                navUpWithResult(navigationCommand)
            }

            ComposeNavigationCommand.PopBackStack -> {
                popBackStack()
            }
        }
    }

    private fun NavController.navUpWithResult(
        navigationCommand: ComposeNavigationCommand.NavigateUpWithResult<*>
    ) {
        val backStackEntry =
            navigationCommand.route?.let { getBackStackEntry(it) }
                ?: previousBackStackEntry
        backStackEntry?.savedStateHandle?.set(
            navigationCommand.key,
            navigationCommand.result
        )

        navigationCommand.route?.let {
            popBackStack(it, false)
        } ?: run {
            navigateUp()
        }
    }

    fun canNavUp(navController: NavController): Boolean = navController.backQueue.isNotEmpty()
}
