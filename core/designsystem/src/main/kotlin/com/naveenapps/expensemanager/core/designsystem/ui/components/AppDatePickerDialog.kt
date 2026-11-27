package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.naveenapps.expensemanager.core.common.utils.toExactStartOfTheDayInstant
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonVariant
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.foundation.fromLocalToUTCTimeStamp
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun AppDatePickerDialog(
    selectedDate: Instant,
    onDateSelected: (Instant) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toEpochMilliseconds().fromLocalToUTCTimeStamp(),
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(EmTheme.spacing.lg),
            shape = EmTheme.shapes.xl,
            color = EmTheme.colors.scheme.surfaceContainerLowest,
            tonalElevation = 6.dp,
        ) {
            Column {
                // Date Picker
                DatePicker(
                    state = datePickerState,
                    showModeToggle = true,
                    colors = DatePickerDefaults.colors(
                        containerColor = EmTheme.colors.scheme.surface,
                    ),
                )

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.lg),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EmButton(
                        text = stringResource(id = R.string.cancel),
                        onClick = onDismiss,
                        variant = EmButtonVariant.Tertiary,
                    )

                    Spacer(modifier = Modifier.width(EmTheme.spacing.sm))

                    EmButton(
                        text = stringResource(id = R.string.select),
                        onClick = {
                            val date = datePickerState.selectedDateMillis?.toExactStartOfTheDayInstant()
                                ?: Clock.System.now()
                            onDateSelected(date)
                        },
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun AppDatePickerDialogPreview() {
    ExpenseManagerPreviewTheme {
        AppDatePickerDialog(
            selectedDate = Clock.System.now(),
            onDateSelected = {},
        ) {}
    }
}
