package com.naveenapps.expensemanager.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppDialog

@Composable
fun DeleteDialogItem(
    confirm: () -> Unit,
    dismiss: () -> Unit,
) {
    AppDialog(
        onDismissRequest = dismiss,
        onConfirmation = confirm,
        dialogTitle = stringResource(id = R.string.delete),
        dialogText = stringResource(id = R.string.delete_item_message),
        positiveButtonText = stringResource(id = R.string.delete),
        negativeButtonText = stringResource(id = R.string.cancel),
    )
}