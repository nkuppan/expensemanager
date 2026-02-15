package com.naveenapps.expensemanager.core.designsystem.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.R

val colors = listOf(
    // Reds
    "#F44336",
    "#E53935",
    "#D32F2F",
    "#B71C1C",

    // Pinks
    "#E91E63",
    "#D81B60",
    "#C2185B",
    "#AD1457",

    // Purples
    "#9C27B0",
    "#8E24AA",
    "#7B1FA2",
    "#6A1B9A",

    // Deep Purples
    "#673AB7",
    "#5E35B1",
    "#512DA8",
    "#4527A0",

    // Indigo
    "#3F51B5",
    "#3949AB",
    "#303F9F",
    "#283593",

    // Blues
    "#2196F3",
    "#1E88E5",
    "#1976D2",
    "#1565C0",

    // Teal
    "#009688",
    "#00897B",
    "#00796B",
    "#00695C",

    // Greens
    "#4CAF50",
    "#43A047",
    "#388E3C",
    "#2E7D32",

    // Lime / Yellow-Green
    "#7CB342",
    "#689F38",
    "#558B2F",
    "#33691E",

    // Orange
    "#FF9800",
    "#FB8C00",
    "#F57C00",
    "#EF6C00",

    // Deep Orange
    "#FF5722",
    "#F4511E",
    "#E64A19",
    "#D84315",

    // Brown
    "#795548",
    "#6D4C41",
    "#5D4037",
    "#4E342E",

    // Blue Grey
    "#607D8B",
    "#546E7A",
    "#455A64",
    "#37474F",

    // Grey / Neutral
    "#757575",
    "#616161",
    "#424242",
    "#212121",
)

@Composable
fun ColorSelectionScreen(
    selectedColorValue: Int? = null,
    onColorPicked: ((Int) -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        // Header
        Text(
            text = stringResource(id = R.string.choose_color),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        // Selected color preview
        selectedColorValue?.let { colorInt ->
            val previewColor = Color(colorInt)
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(previewColor)
                            .then(
                                if (previewColor.luminance() > 0.9f) {
                                    Modifier.border(
                                        1.dp,
                                        Color.Black.copy(alpha = 0.12f),
                                        CircleShape
                                    )
                                } else {
                                    Modifier
                                }
                            ),
                    )
                    Text(
                        text = String.format("#%06X", 0xFFFFFF and colorInt).uppercase(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Color grid
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            columns = GridCells.Fixed(5),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(colors, key = { it }) { colorString ->
                val parsedColorInt = colorString.toColorInt()
                val itemColor = Color(parsedColorInt)
                val isSelected = parsedColorInt == selectedColorValue
                val isLight = itemColor.luminance() > 0.85f

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 0.85f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                    label = "scale",
                )

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(CircleShape)
                        .background(itemColor)
                        .then(
                            if (isLight) {
                                Modifier.border(
                                    1.dp,
                                    Color.Black.copy(alpha = 0.12f),
                                    CircleShape,
                                )
                            } else {
                                Modifier
                            }
                        )
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    2.5.dp,
                                    MaterialTheme.colorScheme.outline,
                                    CircleShape,
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clickable { onColorPicked?.invoke(parsedColorInt) },
                    contentAlignment = Alignment.Center,
                ) {
                    val checkAlpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0f,
                        animationSpec = tween(200),
                        label = "check_alpha",
                    )

                    if (checkAlpha > 0f) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = if (itemColor.luminance() > 0.5f) Color.Black else Color.White,
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer { alpha = checkAlpha },
                        )
                    }
                }
            }

            item(span = { GridItemSpan(this.maxLineSpan) }) {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun ColorSelectionPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        ColorSelectionScreen(
            selectedColorValue = colors[2].toColorInt(), // Example: Selects "#D32F2F"
            onColorPicked = {}
        )
    }
}
