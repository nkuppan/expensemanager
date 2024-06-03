package com.naveenapps.expensemanager.feature.about

import androidx.lifecycle.ViewModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AboutUsViewModel @Inject constructor(
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
