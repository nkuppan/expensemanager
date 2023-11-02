package com.nkuppan.expensemanager.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.ReminderTimeState
import com.nkuppan.expensemanager.ui.theme.ExpenseManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimePickerDialog(
    reminderTimeState: ReminderTimeState,
    onTimeSelected: (ReminderTimeState) -> Unit,
    onDismiss: () -> Unit,
) {

    val timePickerState = rememberTimePickerState(
        initialHour = reminderTimeState.hour,
        initialMinute = reminderTimeState.minute,
        is24Hour = reminderTimeState.is24Hour
    )

    Dialog(
        onDismissRequest = {
            onDismiss.invoke()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
    ) {
        Surface(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.wrapContentSize()) {
                TimePicker(
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                    state = timePickerState,
                )

                Row(
                    modifier = Modifier.align(Alignment.End)
                ) {
                    TextButton(onClick = {
                        onDismiss.invoke()
                    }) {
                        Text(text = stringResource(id = R.string.cancel).uppercase())
                    }
                    TextButton(onClick = {
                        onTimeSelected.invoke(
                            ReminderTimeState(
                                timePickerState.hour,
                                timePickerState.minute,
                                timePickerState.is24hour,
                            )
                        )
                    }) {
                        Text(text = stringResource(id = R.string.select).uppercase())
                    }
                }
            }
        }
    }
}


@Composable
@Preview
fun AppTimePickerDialogPreview() {
    ExpenseManagerTheme {
        AppTimePickerDialog(
            reminderTimeState = ReminderTimeState(10, 0, false),
            onTimeSelected = {},
        ) {}
    }
}