package com.naveenapps.expensemanager.core.navigation

import androidx.navigation.NavController
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

    abstract fun navigate(route: Any)

    abstract fun <T> navigateUpWithResult(key: String, result: T, route: String?)

    abstract fun <T> navigateBackWithResult(key: String, result: T)

    abstract fun navigateBackWithMultipleResult(values: MutableMap<String, Any>)

    abstract fun popUpTo(route: String, inclusive: Boolean)

    abstract fun navigateAndClearBackStack(route: Any)

    abstract fun popBackStack()

    suspend fun handleNavigationCommands(navController: NavController) {
        navigationCommands
            .onSubscription { this@AppComposeNavigator.navControllerFlow.value = navController }
            .onCompletion { this@AppComposeNavigator.navControllerFlow.value = null }
            .collect { navController.handleComposeNavigationCommand(it) }
    }

    private fun NavController.handleComposeNavigationCommand(navigationCommand: NavigationCommand) {
        when (navigationCommand) {
            is NavigationCommand.NavigateToRoute -> {
                navigate(navigationCommand.route, navigationCommand.options)
            }

            NavigationCommand.NavigateUp -> navigateUp()
            is NavigationCommand.PopUpToRoute -> popBackStack(
                navigationCommand.route,
                navigationCommand.inclusive,
            )

            is NavigationCommand.NavigateUpWithResult<*> -> {
                navUpWithResult(navigationCommand)
            }

            NavigationCommand.PopBackStack -> {
                popBackStack()
            }

            is NavigationCommand.NavigateBackWithResult<*> -> {
                previousBackStackEntry?.savedStateHandle?.set(
                    navigationCommand.key,
                    navigationCommand.result,
                )
                popBackStack()
            }

            is NavigationCommand.NavigateBackWithMultipleResult -> {
                navigationCommand.values.forEach {
                    previousBackStackEntry?.savedStateHandle?.set(
                        it.key,
                        it.value,
                    )
                }
                popBackStack()
            }

            is NavigationCommand.NavigateTo -> {
                navigate(navigationCommand.route)
            }
        }
    }

    private fun NavController.navUpWithResult(
        navigationCommand: NavigationCommand.NavigateUpWithResult<*>,
    ) {
        val backStackEntry =
            navigationCommand.route?.let { getBackStackEntry(it) }
                ?: previousBackStackEntry
        backStackEntry?.savedStateHandle?.set(
            navigationCommand.key,
            navigationCommand.result,
        )

        navigationCommand.route?.let {
            popBackStack(it, false)
        } ?: run {
            navigateUp()
        }
    }
}

