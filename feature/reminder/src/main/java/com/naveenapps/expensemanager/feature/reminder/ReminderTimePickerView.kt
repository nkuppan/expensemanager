package com.naveenapps.expensemanager.feature.reminder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppTimePickerDialog
import com.naveenapps.expensemanager.core.model.ReminderTimeState

@Composable
fun ReminderTimePickerView(
    complete: (Boolean) -> Unit,
) {
    val viewModel: ReminderTimePickerViewModel = koinViewModel()
    val reminderTimeState by viewModel.currentReminderTime.collectAsState()

    AppTimePickerDialog(
        reminderTimeState = Triple(
            reminderTimeState.hour,
            reminderTimeState.minute,
            reminderTimeState.is24Hour,
        ),
        onTimeSelected = {
            viewModel.setReminderTimeState(
                ReminderTimeState(it.first, it.second, it.third),
            )
            complete.invoke(true)
        },
    ) {
        complete.invoke(false)
    }
}
