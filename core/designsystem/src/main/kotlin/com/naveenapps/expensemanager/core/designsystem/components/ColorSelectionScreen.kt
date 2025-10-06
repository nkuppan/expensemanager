package com.naveenapps.expensemanager.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.ui.components.SelectionTitle
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.ColorIconSpecModifier

private val colors = listOf(
    "#FFFFFF",
    "#000000",

    "#D32F2F",
    "#B71C1C",

    "#E91E63",
    "#880E4F",

    "#9C27B0",
    "#7B1FA2",
    "#4A148C",

    "#673AB7",
    "#512DA8",
    "#311B92",

    "#3F51B5",
    "#303F9F",
    "#1A237E",

    "#2196F3",
    "#1976D2",
    "#0D47A1",

    "#03A9F4",
    "#0288D1",
    "#01579B",

    "#00BCD4",
    "#0097A7",
    "#006064",

    "#009688",
    "#00796B",
    "#004D40",

    "#4CAF50",
    "#388E3C",
    "#1B5E20",

    "#8BC34A",
    "#689F38",
    "#33691E",

    "#CDDC39",
    "#AFB42B",
    "#827717",

    "#FFEB3B",
    "#FBC02D",
    "#F57F17",

    "#FF9800",
    "#F57C00",
    "#E65100",

    "#795548",
    "#5D4037",
    "#3E2723",

    "#607D8B",
    "#455A64",
    "#263238",
)

@Composable
fun ColorSelectionScreen(
    selectedColorValue: Int? = null,
    onColorPicked: ((Int) -> Unit)? = null
) {
    val columns = GridCells.Adaptive(minSize = 48.dp)
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        columns = columns,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(span = {
            GridItemSpan(this.maxLineSpan)
        }) {
            SelectionTitle(
                stringResource(id = R.string.choose_color),
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp), // Adjusted padding
            )
        }
        items(colors, key = { it }) { colorString ->
            val parsedColorInt = colorString.toColorInt()
            val itemColor = Color(parsedColorInt)
            val isSelected = parsedColorInt == selectedColorValue

            Box(
                modifier = ColorIconSpecModifier
                    .clickable {
                        onColorPicked?.invoke(parsedColorInt)
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .padding(4.dp) // Reduced padding to make circle larger
                        .fillMaxSize(),
                    onDraw = {
                        val colorCircleRadius = size.minDimension / 2.0f

                        if (colorString == "#FFFFFF") {
                            drawCircle(
                                radius = colorCircleRadius,
                                color = itemColor,
                            )
                            // Border for white, drawn slightly inside
                            drawCircle(
                                color = Color.Black,
                                style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round),
                                radius = colorCircleRadius - (0.5.dp.toPx())
                            )
                        } else {
                            drawCircle(
                                color = itemColor,
                                radius = colorCircleRadius
                            )
                        }
                    }
                )

                if (isSelected) {
                    val checkmarkColor = if (itemColor.luminance() > 0.5f) {
                        Color.Black
                    } else {
                        Color.White
                    }
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = checkmarkColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        item(span = {
            GridItemSpan(this.maxLineSpan)
        }) {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

// Please ensure you have a string resource for this, for example in strings.xml:
// <string name="selected_color_indicator">Selected color</string>

@Preview
@Composable
private fun ColorSelectionPreview() {
    ExpenseManagerTheme {
        ColorSelectionScreen(
            selectedColorValue = colors[2].toColorInt(), // Example: Selects "#D32F2F"
            onColorPicked = {}
        )
    }
}

@Preview
@Composable
private fun ColorSelectionPreviewWhiteSelected() {
    ExpenseManagerTheme {
        ColorSelectionScreen(
            selectedColorValue = colors[0].toColorInt(), // Example: Selects "#FFFFFF"
            onColorPicked = {}
        )
    }
}
