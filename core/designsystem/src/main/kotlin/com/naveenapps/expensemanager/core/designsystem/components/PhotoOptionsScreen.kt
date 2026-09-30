package com.naveenapps.expensemanager.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.R

/**
 * The sheet content shown when the "Photo" card in [IconAndColorComponent] is tapped. Replaces
 * the old icon-only picker: a photo (camera or gallery) and the original icon grid are both
 * reachable from here, since a custom photo and a stock icon are mutually exclusive ways to
 * represent the same category/account.
 */
@Composable
fun PhotoOptionsScreen(
    hasCustomImage: Boolean,
    onTakePhoto: () -> Unit,
    onChooseFromGallery: () -> Unit,
    onPickIcon: () -> Unit,
    onRemovePhoto: () -> Unit,
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
        PhotoOptionRow(
            icon = Icons.Rounded.Face,
            label = stringResource(R.string.pick_an_icon),
            onClick = onPickIcon,
        )
        if (hasCustomImage) {
            PhotoOptionRow(
                icon = Icons.Rounded.Close,
                label = stringResource(R.string.remove_photo),
                onClick = onRemovePhoto,
                tint = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
internal fun PhotoOptionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint)
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = tint)
    }
}

@Preview
@Composable
private fun PhotoOptionsScreenPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        PhotoOptionsScreen(
            hasCustomImage = true,
            onTakePhoto = {},
            onChooseFromGallery = {},
            onPickIcon = {},
            onRemovePhoto = {},
        )
    }
}
