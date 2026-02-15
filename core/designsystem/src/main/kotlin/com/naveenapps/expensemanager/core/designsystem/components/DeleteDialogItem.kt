package com.naveenapps.expensemanager.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteDialogItem(
    confirm: () -> Unit,
    dismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { dismiss.invoke() },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        DeleteDialogContent(confirm, dismiss)
    }
}

@Composable
private fun DeleteDialogContent(
    confirm: () -> Unit,
    dismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp) // Increased padding for a more premium "Widget" feel
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.delete),
            // Using your "Pro" weight for headers
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
        )

        Text(
            text = stringResource(id = R.string.delete_item_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f) // Softer secondary text
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // "Keep" is the primary safe action
            com.naveenapps.designsystem.components.PrimaryButton(
                modifier = Modifier.weight(1f),
                onClick = dismiss
            ) {
                Text(text = stringResource(id = R.string.no_keep))
            }

            // "Delete" is the secondary outlined action, but uses a red/error tint
            com.naveenapps.designsystem.components.SecondaryOutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = confirm,
                // If your custom button supports color overrides:
                // colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(
                    text = stringResource(id = R.string.yes_delete),
                    color = MaterialTheme.colorScheme.error // Using the "Due" red color
                )
            }
        }
    }
}

@Composable
@AppPreviewsLightAndDarkMode
fun DeleteDialogPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        DeleteDialogContent({}, {})
    }
}