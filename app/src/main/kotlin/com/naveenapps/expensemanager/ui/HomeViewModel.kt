package com.naveenapps.expensemanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.settings.reminder.UpdateReminderStatusUseCase
import com.naveenapps.expensemanager.core.notification.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class HomeViewModel(
    private val updateReminderStatusUseCase: UpdateReminderStatusUseCase,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _homeScreenBottomBarItems = MutableStateFlow(HomeScreenBottomBarItems.Home)
    val homeScreenBottomBarItems = _homeScreenBottomBarItems.asStateFlow()

    fun setUISystem(homeScreenBottomBarItems: HomeScreenBottomBarItems) {
        _homeScreenBottomBarItems.value = homeScreenBottomBarItems
    }

    fun turnOnNotification() {
        viewModelScope.launch {
            updateReminderStatusUseCase.invoke(true)
            notificationScheduler.checkAndRestartReminder()
        }
    }
}
