package com.naveenapps.expensemanager.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import java.io.File

private val THUMBNAIL_SIZE = 64.dp

/**
 * A single square photo tile used to show one transaction attachment, with a small remove badge
 * in the corner. Meant to sit in a horizontally scrolling row alongside [AttachmentAddTile].
 */
@Composable
fun AttachmentThumbnail(
    imagePath: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(THUMBNAIL_SIZE)) {
        AsyncImage(
            model = File(imagePath),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(THUMBNAIL_SIZE)
                .clip(RoundedCornerShape(12.dp)),
        )
        // A plain clickable circle rather than IconButton — IconButton enforces a 48dp minimum
        // touch target regardless of the size() passed in, which made this badge spill outside
        // the thumbnail's own bounds and overlap the next item in the row.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(3.dp)
                .size(24.dp)
                .background(MaterialTheme.colorScheme.error, CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onRemove,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.remove_photo),
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/**
 * The "add another attachment" tile — a bordered square with a plus icon, rendered at the end of
 * the attachments row.
 */
@Composable
fun AttachmentAddTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(THUMBNAIL_SIZE)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.add_attachment),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun AttachmentAddTilePreview() {
    ExpenseManagerPreviewTheme(padding = 16.dp) {
        AttachmentAddTile(onClick = {})
    }
}

@Preview
@Composable
private fun AttachmentThumbnailPreview() {
    ExpenseManagerPreviewTheme(padding = 16.dp) {
        AttachmentThumbnail(imagePath = "", onRemove = {})
    }
}
