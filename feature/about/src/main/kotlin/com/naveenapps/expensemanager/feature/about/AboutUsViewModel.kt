package com.naveenapps.expensemanager.feature.about

import androidx.lifecycle.ViewModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator


class AboutUsViewModel(
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    fun processAction(action: AboutAction) {
        when (action) {
            AboutAction.ClosePage -> closePage()
            else -> Unit
        }
    }
}
