package com.naveenapps.expensemanager.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
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
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
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
                .clip(EmTheme.shapes.md),
        )
        // A plain clickable circle rather than IconButton — IconButton enforces a 48dp minimum
        // touch target regardless of the size() passed in, which made this badge spill outside
        // the thumbnail's own bounds and overlap the next item in the row.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(EmTheme.spacing.xs)
                .size(24.dp)
                .background(EmTheme.colors.scheme.error, EmTheme.shapes.pill)
                .border(1.dp, EmTheme.colors.scheme.surface, EmTheme.shapes.pill)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onRemove,
                ),
            contentAlignment = Alignment.Center,
        ) {
            EmIcon(
                icon = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.remove_photo),
                size = EmTheme.icons.sm,
                tint = EmTheme.colors.scheme.onError,
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
            .clip(EmTheme.shapes.md)
            .border(
                width = 1.5.dp,
                color = EmTheme.colors.scheme.outlineVariant,
                shape = EmTheme.shapes.md,
            ),
        contentAlignment = Alignment.Center,
    ) {
        EmIconButton(
            icon = Icons.Rounded.Add,
            contentDescription = stringResource(R.string.add_attachment),
            onClick = onClick,
            tint = EmTheme.colors.scheme.onSurfaceVariant,
        )
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
