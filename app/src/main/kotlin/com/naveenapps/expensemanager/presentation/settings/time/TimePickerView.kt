package com.naveenapps.expensemanager.presentation.settings.time

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppTimePickerDialog
import com.naveenapps.expensemanager.core.model.ReminderTimeState

@Composable
fun TimePickerView(
    complete: () -> Unit
) {

    val viewModel: TimePickerViewModel = hiltViewModel()
    val reminderTimeState by viewModel.currentReminderTime.collectAsState()

    AppTimePickerDialog(
        reminderTimeState = Triple(
            reminderTimeState.hour,
            reminderTimeState.minute,
            reminderTimeState.is24Hour,
        ),
        onTimeSelected = {
            viewModel.setReminderTimeState(
                ReminderTimeState(it.first, it.second, it.third)
            )
            complete.invoke()
        }
    ) {
        complete.invoke()
    }
}