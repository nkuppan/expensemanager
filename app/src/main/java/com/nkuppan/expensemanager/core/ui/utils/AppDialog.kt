package com.nkuppan.expensemanager.core.ui.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AppDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    positiveButtonText: String,
    negativeButtonText: String? = null,
) {
    AlertDialog(
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = dialogTitle
            )
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(positiveButtonText)
            }
        },
        dismissButton = {
            if (negativeButtonText?.isNotBlank() == true) {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(negativeButtonText)
                }
            }
        }
    )
}


@Preview
@Composable
private fun AppDialogPreview() {
    MaterialTheme {
        AppDialog(
            onDismissRequest = { /*TODO*/ },
            onConfirmation = { /*TODO*/ },
            dialogTitle = "Info",
            dialogText = "Do you want to delete this item?",
            positiveButtonText = "Delete",
            negativeButtonText = "Cancel"
        )
    }
}