package com.naveenapps.expensemanager.feature.dashboard.customise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.designsystem.components.swap
import com.naveenapps.expensemanager.core.model.DashboardSection
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardReOrderState(
    val sections: List<DashboardSection> = emptyList(),
    /** True once the order differs from what's saved. */
    val showSaveButton: Boolean = false,
)

sealed class DashboardReOrderAction {
    data object ClosePage : DashboardReOrderAction()

    data object Save : DashboardReOrderAction()

    /** Back to the default order (still needs Save, like any other change). */
    data object Reset : DashboardReOrderAction()

    data class Swap(val fromIndex: Int, val toIndex: Int) : DashboardReOrderAction()
}

/** Customise dashboard: same drag-to-reorder flow as Accounts → Re-order, then Save. */
class DashboardReOrderViewModel(
    private val settingsRepository: SettingsRepository,
    private val appComposeNavigator: AppComposeNavigator,
    private val analyticsRepository: AnalyticsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardReOrderState())
    val state = _state.asStateFlow()

    private var savedOrder: List<DashboardSection> = DashboardSection.DEFAULT_ORDER

    init {
        // Read once: a later emission must not undo a drag in progress.
        viewModelScope.launch {
            savedOrder = settingsRepository.getDashboardOrder().first()
            _state.update { it.copy(sections = savedOrder, showSaveButton = false) }
        }
    }

    private fun update(sections: List<DashboardSection>) {
        _state.update { it.copy(sections = sections, showSaveButton = sections != savedOrder) }
    }

    private fun save() {
        viewModelScope.launch {
            settingsRepository.setDashboardOrder(_state.value.sections)
            analyticsRepository.logEvent(AnalyticsEvents.DASHBOARD_CUSTOMISED, emptyMap())
            appComposeNavigator.popBackStack()
        }
    }

    fun processAction(action: DashboardReOrderAction) {
        when (action) {
            DashboardReOrderAction.ClosePage -> appComposeNavigator.popBackStack()
            DashboardReOrderAction.Save -> save()
            DashboardReOrderAction.Reset -> update(DashboardSection.DEFAULT_ORDER)
            is DashboardReOrderAction.Swap -> update(_state.value.sections.swap(action.fromIndex, action.toIndex))
        }
    }
}
