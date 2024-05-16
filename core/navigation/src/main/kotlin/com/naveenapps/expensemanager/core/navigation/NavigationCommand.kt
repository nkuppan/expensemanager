package com.naveenapps.expensemanager.core.navigation

import androidx.navigation.NavOptions

sealed class NavigationCommand {
    data object NavigateUp : NavigationCommand()
}

sealed class ComposeNavigationCommand : NavigationCommand() {
    data class NavigateTo(val route: Any) : ComposeNavigationCommand()

    data class NavigateToRoute(
        val route: Any,
        val options: NavOptions? = null
    ) : ComposeNavigationCommand()

    data class NavigateUpWithResult<T>(
        val key: String,
        val result: T,
        val route: String? = null,
    ) : ComposeNavigationCommand()

    data class NavigateBackWithResult<T>(
        val key: String,
        val result: T,
    ) : ComposeNavigationCommand()

    data class NavigateBackWithMultipleResult(
        val values: Map<String, Any>
    ) : ComposeNavigationCommand()

    data class PopUpToRoute(val route: String, val inclusive: Boolean) : ComposeNavigationCommand()

    data object PopBackStack : ComposeNavigationCommand()
}
