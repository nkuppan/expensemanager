package com.naveenapps.expensemanager.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme

/**
 * The sheet content shown when adding a transaction attachment. Simpler than [PhotoOptionsScreen]
 * — a transaction can have any number of attachments, so there's no "current photo" state and no
 * icon-picker fallback, just the two ways to bring in a new photo.
 */
@Composable
fun AttachmentPickerScreen(
    onTakePhoto: () -> Unit,
    onChooseFromGallery: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Text(
            text = stringResource(id = R.string.choose_photo),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )

        PhotoOptionRow(
            icon = Icons.Rounded.PhotoCamera,
            label = stringResource(R.string.take_photo),
            onClick = onTakePhoto,
        )
        PhotoOptionRow(
            icon = Icons.Rounded.PhotoLibrary,
            label = stringResource(R.string.choose_from_gallery),
            onClick = onChooseFromGallery,
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview
@Composable
private fun AttachmentPickerScreenPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        AttachmentPickerScreen(
            onTakePhoto = {},
            onChooseFromGallery = {},
        )
    }
}
