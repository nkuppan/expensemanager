package com.naveenapps.expensemanager.presentation.settings.time

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.model.ReminderTimeState
import com.naveenapps.expensemanager.domain.usecase.settings.time.GetReminderTimeUseCase
import com.naveenapps.expensemanager.domain.usecase.settings.time.SaveReminderTimeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimePickerViewModel @Inject constructor(
    getReminderTimeUseCase: GetReminderTimeUseCase,
    private val saveReminderTimeUseCase: SaveReminderTimeUseCase,
) : ViewModel() {

    private val _currentReminderTime = MutableStateFlow(
        ReminderTimeState(10, 0, false)
    )
    val currentReminderTime = _currentReminderTime.asStateFlow()

    init {
        getReminderTimeUseCase.invoke().onEach {
            _currentReminderTime.value = it
        }.launchIn(viewModelScope)
    }

    fun setReminderTimeState(reminderTimeState: ReminderTimeState?) {
        reminderTimeState ?: return
        viewModelScope.launch {
            saveReminderTimeUseCase.invoke(reminderTimeState)
        }
    }
}
