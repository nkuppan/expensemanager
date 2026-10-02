package com.naveenapps.expensemanager.core.navigation

import androidx.lifecycle.Lifecycle
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

    /** Replaces the back stack with [routes], e.g. Home then TransactionCreate on top. */
    abstract fun resetBackStackTo(routes: List<Any>)

    abstract fun popBackStack()

    suspend fun handleNavigationCommands(navController: NavController) {
        navigationCommands
            .onSubscription { this@AppComposeNavigator.navControllerFlow.value = navController }
            .onCompletion { this@AppComposeNavigator.navControllerFlow.value = null }
            .collect { navController.handleComposeNavigationCommand(it) }
    }

    private fun NavController.handleComposeNavigationCommand(navigationCommand: NavigationCommand) {
        // Guard against a navigation command firing twice for the same in-flight transition —
        // e.g. a fast double-tap on a toolbar back arrow, or the system back gesture racing
        // with our own popBackStack()/navigate() call. Once a transition starts, the current
        // entry's lifecycle moves away from RESUMED before it's actually removed from the back
        // stack, so a redundant command arriving mid-transition is safely ignored here instead
        // of crashing with IllegalStateException from NavControllerImpl.
        if (!isCurrentEntryResumed()) return

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

            is NavigationCommand.ResetBackStack -> {
                navigationCommand.routes.forEachIndexed { index, route ->
                    if (index == 0) {
                        navigate(route) { popUpTo(0) }
                    } else {
                        navigate(route)
                    }
                }
            }
        }
    }

    private fun NavController.isCurrentEntryResumed(): Boolean = currentBackStackEntry?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) ?: true

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
