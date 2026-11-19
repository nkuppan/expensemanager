package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimePickerDialog(
    reminderTimeState: Triple<Int, Int, Boolean>,
    onTimeSelected: (Triple<Int, Int, Boolean>) -> Unit,
    onDismiss: () -> Unit,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = reminderTimeState.first,
        initialMinute = reminderTimeState.second,
        is24Hour = reminderTimeState.third,
    )

    Dialog(
        onDismissRequest = {
            onDismiss.invoke()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        EmCard(modifier = Modifier.padding(EmTheme.spacing.lg)) {
            Column(modifier = Modifier.wrapContentSize()) {
                TimePicker(
                    modifier = Modifier.padding(start = EmTheme.spacing.lg, top = EmTheme.spacing.lg, end = EmTheme.spacing.lg),
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        containerColor = EmTheme.colors.scheme.surface,
                    ),
                )

                Row(
                    modifier = Modifier.align(Alignment.End),
                ) {
                    EmButton(
                        text = stringResource(id = R.string.cancel),
                        onClick = {
                        onDismiss.invoke()
                    },
                        variant = EmButtonVariant.Tertiary,
                    )
                    EmButton(
                        text = stringResource(id = R.string.select),
                        onClick = {
                        onTimeSelected.invoke(
                            Triple(
                                timePickerState.hour,
                                timePickerState.minute,
                                timePickerState.is24hour,
                            ),
                        )
                    },
                        variant = EmButtonVariant.Tertiary,
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun AppTimePickerDialogPreview() {
    ExpenseManagerPreviewTheme {
        AppTimePickerDialog(
            reminderTimeState = Triple(10, 0, false),
            onTimeSelected = {},
        ) {}
    }
}
