package com.nkuppan.expensemanager.core.ui.utils

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.nkuppan.expensemanager.core.ui.theme.ExpenseManagerTheme
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.time
    )

    DatePickerDialog(
        modifier = modifier,
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(onClick = {
                val currentDate = datePickerState.selectedDateMillis?.let {
                    Date(it)
                } ?: selectedDate
                onDateSelected(currentDate)
                onDismiss()
            }

            ) {
                Text(text = "OK")
            }
        },
        dismissButton = {
            Button(onClick = {
                onDismiss()
            }) {
                Text(text = "Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState
        )
    }
}


@Composable
@Preview
fun AppDatePickerDialogPreview() {
    ExpenseManagerTheme {
        AppDatePickerDialog(
            selectedDate = Date(),
            modifier = Modifier.wrapContentSize(),
            onDateSelected = {},
            onDismiss = {},
        )
    }
}