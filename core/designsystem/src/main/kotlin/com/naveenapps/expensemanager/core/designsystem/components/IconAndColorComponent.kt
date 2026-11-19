package com.naveenapps.expensemanager.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.naveenapps.expensemanager.core.common.utils.toColorString
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmCardVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmModalSheet
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.getDrawable
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.toColor
import java.io.File

enum class SelectionType {
    NONE,
    COLOR_SELECTION,
    PHOTO_OPTIONS,
    ICON_SELECTION,
}

@Composable
fun IconAndColorComponent(
    selectedColor: String,
    selectedIcon: String,
    onColorSelection: ((String) -> Unit)?,
    onIconSelection: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
    customImagePath: String? = null,
    onCaptureRequested: (() -> Unit)? = null,
    onGalleryRequested: (() -> Unit)? = null,
    onRemoveImage: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val focusManager = LocalFocusManager.current
    var sheetSelection by remember { mutableStateOf(SelectionType.NONE) }

    if (sheetSelection != SelectionType.NONE) {
        EmModalSheet(
            onDismissRequest = {
                sheetSelection = SelectionType.NONE
            },
        ) {
            when (sheetSelection) {
                SelectionType.COLOR_SELECTION -> {
                    ColorSelectionScreen {
                        onColorSelection?.invoke(it.toColorString())
                        sheetSelection = SelectionType.NONE
                    }
                }

                SelectionType.PHOTO_OPTIONS -> {
                    PhotoOptionsScreen(
                        hasCustomImage = customImagePath != null,
                        onTakePhoto = {
                            sheetSelection = SelectionType.NONE
                            onCaptureRequested?.invoke()
                        },
                        onChooseFromGallery = {
                            sheetSelection = SelectionType.NONE
                            onGalleryRequested?.invoke()
                        },
                        onPickIcon = {
                            // Stay open — swap this same sheet's content to the icon grid rather
                            // than closing and reopening, so it reads as one continuous flow.
                            sheetSelection = SelectionType.ICON_SELECTION
                        },
                        onRemovePhoto = {
                            sheetSelection = SelectionType.NONE
                            onRemoveImage?.invoke()
                        },
                    )
                }

                SelectionType.ICON_SELECTION -> {
                    IconSelectionScreen {
                        onIconSelection?.invoke(resources.getResourceName(it))
                        sheetSelection = SelectionType.NONE
                    }
                }

                else -> Unit
            }
        }
    }

    val containerColor = selectedColor.toColor()
    val contentColor = colorResource(id = android.R.color.white)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.md),
    ) {
        Surface(
            shape = EmTheme.shapes.lg,
            tonalElevation = 2.dp,
            modifier = Modifier.weight(1f),
            onClick = {
                sheetSelection = SelectionType.COLOR_SELECTION
                focusManager.clearFocus(force = true)
            },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = EmTheme.spacing.md, vertical = EmTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(EmTheme.shapes.pill)
                        .background(containerColor)
                        .border(
                            width = 1.5.dp,
                            color = EmTheme.colors.scheme.outlineVariant,
                            shape = EmTheme.shapes.pill,
                        ),
                )
                EmText(
                    text = stringResource(R.string.color),
                    modifier = Modifier.weight(1f),
                )
                EmIcon(
                    icon = Icons.Rounded.Edit,
                    contentDescription = null,
                    size = EmTheme.icons.sm,
                    tint = EmTheme.colors.scheme.onSurfaceVariant,
                )
            }
        }

        // Photo/icon picker — replaces the old icon-only card. Shows the custom photo when one is
        // set, otherwise falls back to today's icon+color preview.
        EmCard(
            modifier = Modifier.weight(1f),
            variant = EmCardVariant.Filled,
            onClick = {
                sheetSelection = SelectionType.PHOTO_OPTIONS
                focusManager.clearFocus(force = true)
            },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = EmTheme.spacing.md, vertical = EmTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
            ) {
                if (customImagePath != null) {
                    AsyncImage(
                        model = File(customImagePath),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(EmTheme.shapes.pill),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(EmTheme.shapes.pill)
                            .background(containerColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        EmIcon(
                            painter = painterResource(context.getDrawable(iconName = selectedIcon)),
                            contentDescription = null,
                            size = EmTheme.icons.sm,
                            tint = contentColor,
                        )
                    }
                }
                EmText(
                    text = stringResource(R.string.photo),
                    modifier = Modifier.weight(1f),
                )
                EmIcon(
                    icon = Icons.Rounded.Edit,
                    contentDescription = null,
                    size = EmTheme.icons.sm,
                    tint = EmTheme.colors.scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
@EmPreviews
private fun IconAndColorComponentPreview() {
    ExpenseManagerPreviewTheme(padding = 16.dp) {
        IconAndColorComponent(
            selectedColor = "#000000",
            selectedIcon = "account_balance",
            onColorSelection = {},
            onIconSelection = {},
        )
    }
}
