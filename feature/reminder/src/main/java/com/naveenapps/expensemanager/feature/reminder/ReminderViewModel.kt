package com.naveenapps.expensemanager.feature.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.fromTimeAndHour
import com.naveenapps.expensemanager.core.common.utils.toTimeAndMinutesWithAMPM
import com.naveenapps.expensemanager.core.domain.usecase.settings.reminder.GetReminderStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.reminder.GetReminderTimeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.reminder.UpdateReminderStatusUseCase
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    getReminderTimeUseCase: GetReminderTimeUseCase,
    getReminderStatusUseCase: GetReminderStatusUseCase,
    private val updateReminderStatusUseCase: UpdateReminderStatusUseCase,
    private val notificationScheduler: NotificationScheduler,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _reminderOn = MutableStateFlow(false)
    val reminderOn = _reminderOn.asStateFlow()

    private val _reminderTime = MutableStateFlow("06:00")
    val reminderTime = _reminderTime.asStateFlow()

    init {
        getReminderStatusUseCase.invoke().onEach {
            _reminderOn.value = it
        }.launchIn(viewModelScope)

        getReminderTimeUseCase.invoke().onEach {
            _reminderTime.value =
                "${it.hour}:${it.minute}".fromTimeAndHour().toTimeAndMinutesWithAMPM()
        }.launchIn(viewModelScope)
    }

    fun saveReminderStatus(status: Boolean) {
        viewModelScope.launch {
            updateReminderStatusUseCase.invoke(status)

            if (status) {
                notificationScheduler.setReminder()
            } else {
                notificationScheduler.cancelReminder()
            }
        }
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }
}
