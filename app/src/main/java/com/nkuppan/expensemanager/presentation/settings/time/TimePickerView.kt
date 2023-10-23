package com.nkuppan.expensemanager.presentation.settings.time

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.nkuppan.expensemanager.common.ui.theme.widget.AppTimePickerDialog

@Composable
fun TimePickerView(
    complete: () -> Unit
) {

    val viewModel: TimePickerViewModel = hiltViewModel()
    val reminderTimeState by viewModel.currentReminderTime.collectAsState()

    AppTimePickerDialog(
        reminderTimeState = reminderTimeState,
        onTimeSelected = {
            viewModel.setReminderTimeState(it)
            complete.invoke()
        }
    ) {
        complete.invoke()
    }
}