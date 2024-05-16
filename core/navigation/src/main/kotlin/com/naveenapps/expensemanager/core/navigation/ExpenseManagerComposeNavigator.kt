package com.naveenapps.expensemanager.core.navigation

import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions
import javax.inject.Inject

class ExpenseManagerComposeNavigator @Inject constructor() : AppComposeNavigator() {

    override fun navigate(route: Any) {
        navigationCommands.tryEmit(ComposeNavigationCommand.NavigateTo(route))
    }

    override fun navigateAndClearBackStack(route: Any) {
        navigationCommands.tryEmit(
            ComposeNavigationCommand.NavigateToRoute(
                route,
                navOptions {
                    popUpTo(0)
                },
            ),
        )
    }

    override fun popBackStack() {
        navigationCommands.tryEmit(ComposeNavigationCommand.PopBackStack)
    }

    override fun popUpTo(route: String, inclusive: Boolean) {
        navigationCommands.tryEmit(ComposeNavigationCommand.PopUpToRoute(route, inclusive))
    }

    override fun <T> navigateUpWithResult(
        key: String,
        result: T,
        route: String?,
    ) {
        navigationCommands.tryEmit(
            ComposeNavigationCommand.NavigateUpWithResult(
                key = key,
                result = result,
                route = route,
            ),
        )
    }

    override fun <T> navigateBackWithResult(key: String, result: T) {
        navigationCommands.tryEmit(
            ComposeNavigationCommand.NavigateBackWithResult(
                key = key,
                result = result,
            ),
        )
    }

    override fun navigateBackWithMultipleResult(values: MutableMap<String, Any>) {
        navigationCommands.tryEmit(
            ComposeNavigationCommand.NavigateBackWithMultipleResult(values = values)
        )
    }
}
