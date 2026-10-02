package com.naveenapps.expensemanager.core.navigation

import androidx.navigation.NavOptions

sealed class NavigationCommand {

    data object NavigateUp : NavigationCommand()

    data class NavigateTo(val route: Any) : NavigationCommand()

    /**
     * Clears the whole back stack, then pushes [routes] in order (first = new root). Done as one
     * command because the handler drops commands that arrive mid-transition, so two separate
     * navigate calls in a row would lose the second one.
     */
    data class ResetBackStack(val routes: List<Any>) : NavigationCommand()

    data class NavigateToRoute(
        val route: Any,
        val options: NavOptions? = null,
    ) : NavigationCommand()

    data class NavigateUpWithResult<T>(
        val key: String,
        val result: T,
        val route: String? = null,
    ) : NavigationCommand()

    data class NavigateBackWithResult<T>(
        val key: String,
        val result: T,
    ) : NavigationCommand()

    data class NavigateBackWithMultipleResult(
        val values: Map<String, Any>,
    ) : NavigationCommand()

    data class PopUpToRoute(val route: String, val inclusive: Boolean) : NavigationCommand()

    data object PopBackStack : NavigationCommand()
}
