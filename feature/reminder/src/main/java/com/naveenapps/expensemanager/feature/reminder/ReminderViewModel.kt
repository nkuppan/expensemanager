package com.naveenapps.expensemanager.feature.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.fromTimeAndHour
import com.naveenapps.expensemanager.core.common.utils.toTimeAndMinutesWithAMPM
import com.naveenapps.expensemanager.core.domain.usecase.settings.reminder.GetReminderStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.reminder.GetReminderTimeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.reminder.SaveReminderTimeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.reminder.UpdateReminderStatusUseCase
import com.naveenapps.expensemanager.core.model.ReminderTimeState
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.notification.NotificationScheduler
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.ReminderTimeRepository
import com.naveenapps.expensemanager.core.repository.VersionCheckerRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReminderViewModel(
    getReminderTimeUseCase: GetReminderTimeUseCase,
    getReminderStatusUseCase: GetReminderStatusUseCase,
    versionCheckerRepository: VersionCheckerRepository,
    private val updateReminderStatusUseCase: UpdateReminderStatusUseCase,
    private val notificationScheduler: NotificationScheduler,
    private val appComposeNavigator: AppComposeNavigator,
    private val saveReminderTimeUseCase: SaveReminderTimeUseCase,
    private val reminderTimeRepository: ReminderTimeRepository,
    private val analyticsRepository: AnalyticsRepository,
) : ViewModel() {

    private val _event = Channel<ReminderEvent>()
    val event = _event.receiveAsFlow()

    private val _state = MutableStateFlow(
        ReminderState(
            showPermissionMessage = versionCheckerRepository.isAndroidTiramisuAndAbove(),
        ),
    )
    val state = _state.asStateFlow()

    init {
        getReminderStatusUseCase.invoke().onEach { status ->
            _state.update { it.copy(reminderStatus = status) }
        }.launchIn(viewModelScope)

        reminderTimeRepository.isUpcomingAlertOn().onEach { on ->
            _state.update { it.copy(upcomingAlertsOn = on) }
        }.launchIn(viewModelScope)

        getReminderTimeUseCase.invoke().onEach { time ->
            _state.update {
                it.copy(
                    reminderTimeState = time,
                    reminderTime = "${time.hour}:${time.minute}".fromTimeAndHour()
                        .toTimeAndMinutesWithAMPM(),
                )
            }
        }.launchIn(viewModelScope)
    }

    private suspend fun refreshDelivery() {
        val health = notificationScheduler.reminderHealth()
        _state.update {
            it.copy(
                lastReminderRunAt = health.lastRunAt,
                deliveryBlock = health.block,
                isBatteryOptimised = health.isBatteryOptimised,
            )
        }
    }

    private fun saveReminderStatus(status: Boolean) {
        viewModelScope.launch {
            updateReminderStatusUseCase.invoke(status)

            if (status) {
                notificationScheduler.setReminder()
            } else {
                notificationScheduler.cancelReminder()
            }

            _state.update {
                it.copy(
                    showPermissionMessage = false,
                )
            }
        }
    }

    // Independent of the daily reminder: either can be on without the other.
    private fun saveUpcomingAlerts(on: Boolean) {
        viewModelScope.launch {
            reminderTimeRepository.setUpcomingAlertOn(on)
            if (on) {
                notificationScheduler.scheduleUpcomingAlert()
            } else {
                notificationScheduler.cancelUpcomingAlert()
            }
            analyticsRepository.logEvent(
                if (on) AnalyticsEvents.UPCOMING_ALERTS_ON else AnalyticsEvents.UPCOMING_ALERTS_OFF,
                emptyMap(),
            )
        }
    }

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    private fun setReminderTimeState(reminderTimeState: ReminderTimeState?) {
        reminderTimeState ?: return
        viewModelScope.launch {
            saveReminderTimeUseCase.invoke(reminderTimeState)
            notificationScheduler.setReminder()
        }
    }

    fun processAction(action: ReminderAction) {
        viewModelScope.launch {
            when (action) {
                ReminderAction.ClosePage -> closePage()

                ReminderAction.CloseReminderDialog -> {
                    _state.update { it.copy(showTimePickerDialog = false) }
                }

                is ReminderAction.SaveReminder -> {
                    _state.update { it.copy(showTimePickerDialog = false) }
                    setReminderTimeState(action.reminderState)
                }

                is ReminderAction.ChangeUpcomingAlerts -> saveUpcomingAlerts(action.on)

                is ReminderAction.ChangeReminderStatus -> {
                    saveReminderStatus(action.status)
                }

                ReminderAction.RequestPermission -> {
                    _event.send(ReminderEvent.RequestPermission)
                }

                ReminderAction.ShowPermissionRationale -> {
                    _state.update {
                        it.copy(
                            showPermissionMessage = true,
                            shouldShowRationale = true,
                        )
                    }
                }

                ReminderAction.OpenSettings -> {
                    _event.send(ReminderEvent.OpenSettings)
                }

                ReminderAction.ShowReminderDialog -> {
                    if (_state.value.reminderStatus) {
                        _state.update { it.copy(showTimePickerDialog = true) }
                    }
                }

                ReminderAction.RefreshDelivery -> refreshDelivery()

                is ReminderAction.DeliveryFixOpened -> analyticsRepository.logEvent(
                    AnalyticsEvents.REMINDER_FIX_OPENED,
                    mapOf(AnalyticsParams.REASON to action.reason),
                )

                ReminderAction.PermissionGranted -> {
                    _state.update {
                        it.copy(showPermissionMessage = false)
                    }
                }
            }
        }
    }
}
