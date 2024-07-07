package com.naveenapps.expensemanager.core.navigation

import androidx.navigation.NavOptions

sealed class NavigationCommand {

    data object NavigateUp : NavigationCommand()

    data class NavigateTo(val route: Any) : NavigationCommand()

    data class NavigateToRoute(
        val route: Any,
        val options: NavOptions? = null
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
        val values: Map<String, Any>
    ) : NavigationCommand()

    data class PopUpToRoute(val route: String, val inclusive: Boolean) : NavigationCommand()

    data object PopBackStack : NavigationCommand()
}
