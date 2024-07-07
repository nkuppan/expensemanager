package com.naveenapps.expensemanager.core.navigation

import androidx.navigation.navOptions
import javax.inject.Inject

class ExpenseManagerComposeNavigator @Inject constructor() : AppComposeNavigator() {

    override fun navigate(route: Any) {
        navigationCommands.tryEmit(NavigationCommand.NavigateTo(route))
    }

    override fun navigateAndClearBackStack(route: Any) {
        navigationCommands.tryEmit(
            NavigationCommand.NavigateToRoute(
                route,
                navOptions {
                    popUpTo(0)
                },
            ),
        )
    }

    override fun popBackStack() {
        navigationCommands.tryEmit(NavigationCommand.PopBackStack)
    }

    override fun popUpTo(route: String, inclusive: Boolean) {
        navigationCommands.tryEmit(NavigationCommand.PopUpToRoute(route, inclusive))
    }

    override fun <T> navigateUpWithResult(
        key: String,
        result: T,
        route: String?,
    ) {
        navigationCommands.tryEmit(
            NavigationCommand.NavigateUpWithResult(
                key = key,
                result = result,
                route = route,
            ),
        )
    }

    override fun <T> navigateBackWithResult(key: String, result: T) {
        navigationCommands.tryEmit(
            NavigationCommand.NavigateBackWithResult(
                key = key,
                result = result,
            ),
        )
    }

    override fun navigateBackWithMultipleResult(values: MutableMap<String, Any>) {
        navigationCommands.tryEmit(
            NavigationCommand.NavigateBackWithMultipleResult(values = values)
        )
    }
}
