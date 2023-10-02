package com.nkuppan.expensemanager.presentation.settings.time

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nkuppan.expensemanager.core.ui.theme.widget.AppTimePickerDialog

@Composable
fun TimePickerView(
    complete: () -> Unit
) {

    val viewModel: TimePickerViewModel = hiltViewModel()
    val reminderTimeState by viewModel.currentReminderTime.collectAsState()

    AppTimePickerDialog(
        modifier = Modifier
            .wrapContentSize()
            .padding(16.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(8.dp)
            ),
        reminderTimeState = reminderTimeState,
        onTimeSelected = {
            viewModel.setReminderTimeState(it)
            complete.invoke()
        },
        onDismiss = {
            complete.invoke()
        }
    )
}