package com.naveenapps.expensemanager.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.ui.components.PrimaryButton
import com.naveenapps.expensemanager.core.designsystem.ui.components.SecondaryOutlinedButton
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteDialogItem(
    confirm: () -> Unit,
    dismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { dismiss.invoke() },
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
    ) {
        DeleteDialogContent(confirm, dismiss)
    }
}

@Composable
private fun DeleteDialogContent(
    confirm: () -> Unit,
    dismiss: () -> Unit,
) {
    val bottom = WindowInsets.systemBars.getBottom(LocalDensity.current).fromIntToDp()
    Surface(
        Modifier.padding(bottom = bottom)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    modifier = Modifier,
                    text = stringResource(id = R.string.delete),
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Text(
                modifier = Modifier,
                text = stringResource(id = R.string.delete_item_message),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PrimaryButton(
                    modifier = Modifier.weight(1f),
                    onClick = dismiss
                ) {
                    Text(text = stringResource(id = R.string.no_keep))
                }
                SecondaryOutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = confirm
                ) {
                    Text(text = stringResource(id = R.string.yes_delete))
                }
            }
        }
    }
}

@Composable
@AppPreviewsLightAndDarkMode
fun DeleteDialogPreview() {
    ExpenseManagerTheme {
        DeleteDialogContent({}, {})
    }
}

@Composable
fun Int.fromIntToDp(): Dp {
    return with(LocalDensity.current) { this@fromIntToDp.toDp() }
}