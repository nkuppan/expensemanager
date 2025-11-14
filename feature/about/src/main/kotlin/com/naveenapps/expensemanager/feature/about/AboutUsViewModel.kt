package com.naveenapps.expensemanager.feature.about

import androidx.lifecycle.ViewModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.repository.DevicePropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class AboutUsViewModel(
    devicePropertyRepository: DevicePropertyRepository,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _state = MutableStateFlow(
        AboutUsState(
            appVersion = devicePropertyRepository.getAppVersion()
        )
    )
    val state = _state.asStateFlow()

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
