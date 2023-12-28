package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.naveenapps.expensemanager.core.common.utils.toCompleteDate
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.time)

    Dialog(
        onDismissRequest = {
            onDismiss.invoke()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Column(modifier = Modifier.wrapContentSize()) {
                DatePicker(state = datePickerState)
                Row(
                    modifier = Modifier.align(Alignment.End),
                ) {
                    TextButton(onClick = {
                        onDismiss.invoke()
                    }) {
                        Text(text = stringResource(id = R.string.cancel).uppercase())
                    }
                    TextButton(onClick = {
                        onDateSelected.invoke(
                            datePickerState.selectedDateMillis?.toCompleteDate() ?: Date()
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
fun AppDatePickerDialogPreview() {
    ExpenseManagerTheme {
        AppDatePickerDialog(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp),
            selectedDate = Date(),
            onDateSelected = {},
            onDismiss = {},
        )
    }
}
