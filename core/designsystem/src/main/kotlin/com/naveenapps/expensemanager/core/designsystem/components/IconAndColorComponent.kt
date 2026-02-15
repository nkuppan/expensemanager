package com.naveenapps.expensemanager.core.designsystem.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.common.utils.toColorString
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.getDrawable
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.toColor
import kotlinx.coroutines.launch

enum class SelectionType {
    NONE,
    COLOR_SELECTION,
    ICON_SELECTION,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconAndColorComponent(
    selectedColor: String,
    selectedIcon: String,
    onColorSelection: ((String) -> Unit)?,
    onIconSelection: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val focusManager = LocalFocusManager.current

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = {
            Log.d("******", "IconAndColorComponent: $it")
            true
        },
    )

    val scope = rememberCoroutineScope()
    var sheetSelection by remember { mutableStateOf(SelectionType.NONE) }

    if (sheetSelection != SelectionType.NONE) {
        ModalBottomSheet(
            modifier = Modifier.wrapContentSize(),
            sheetState = sheetState,
            onDismissRequest = {
                sheetSelection = SelectionType.NONE
            },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            when (sheetSelection) {
                SelectionType.COLOR_SELECTION -> {
                    ColorSelectionScreen {
                        onColorSelection?.invoke(it.toColorString())
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                sheetSelection = SelectionType.NONE
                            }
                        }
                    }
                }

                SelectionType.ICON_SELECTION -> {
                    IconSelectionScreen {
                        onIconSelection?.invoke(resources.getResourceName(it))
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                sheetSelection = SelectionType.NONE
                            }
                        }
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
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 2.dp,
            modifier = Modifier.weight(1f),
            onClick = {
                sheetSelection = SelectionType.COLOR_SELECTION
                focusManager.clearFocus(force = true)
            },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(containerColor)
                        .border(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = CircleShape,
                        ),
                )
                Text(
                    text = stringResource(R.string.color),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        // Icon picker
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 2.dp,
            modifier = Modifier.weight(1f),
            onClick = {
                sheetSelection = SelectionType.ICON_SELECTION
                focusManager.clearFocus(force = true)
            },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                IconButton(
                    modifier = Modifier.size(28.dp),
                    onClick = { },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = containerColor,
                        contentColor = contentColor,
                    ),
                ) {
                    Icon(
                        painter = painterResource(context.getDrawable(iconName = selectedIcon)),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.icon),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
@AppPreviewsLightAndDarkMode
private fun IconAndColorComponentPreview() {
    NaveenAppsPreviewTheme(padding = 16.dp) {
        IconAndColorComponent(
            selectedColor = "#000000",
            selectedIcon = "account_balance",
            onColorSelection = {},
            onIconSelection = {},
        )
    }
}
