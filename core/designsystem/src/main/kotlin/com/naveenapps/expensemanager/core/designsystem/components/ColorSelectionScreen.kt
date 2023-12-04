package com.naveenapps.expensemanager.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.ui.components.SelectionTitle
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.ColorIconSpecModifier

private val colors = listOf(
    "#E57373",
    "#F44336",
    "#D32F2F",
    "#B71C1C",

    "#F06292",
    "#E91E63",
    "#C2185B",
    "#880E4F",

    "#BA68C8",
    "#9C27B0",
    "#7B1FA2",
    "#4A148C",

    "#9575CD",
    "#673AB7",
    "#512DA8",
    "#311B92",

    "#7986CB",
    "#3F51B5",
    "#303F9F",
    "#1A237E",

    "#64B5F6",
    "#2196F3",
    "#1976D2",
    "#0D47A1",

    "#4FC3F7",
    "#03A9F4",
    "#0288D1",
    "#01579B",

    "#4DD0E1",
    "#00BCD4",
    "#0097A7",
    "#006064",

    "#4DB6AC",
    "#009688",
    "#00796B",
    "#004D40",

    "#81C784",
    "#4CAF50",
    "#388E3C",
    "#1B5E20",

    "#AED581",
    "#8BC34A",
    "#689F38",
    "#33691E",

    "#DCE775",
    "#CDDC39",
    "#AFB42B",
    "#827717",

    "#FFF176",
    "#FFEB3B",
    "#FBC02D",
    "#F57F17",

    "#FFB74D",
    "#FF9800",
    "#F57C00",
    "#E65100",

    "#A1887F",
    "#795548",
    "#5D4037",
    "#3E2723",

    "#90A4AE",
    "#607D8B",
    "#455A64",
    "#263238",
)

@Composable
fun ColorSelectionScreen(onColorPicked: ((Int) -> Unit)? = null) {
    val columns = GridCells.Adaptive(minSize = 48.dp)
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        columns = columns,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(span = {
            GridItemSpan(this.maxLineSpan)
        }) {
            SelectionTitle(
                stringResource(id = R.string.choose_color),
                Modifier.Companion
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
            )
        }
        items(colors) { color ->
            val parsedColor = color.toColorInt()
            Box(
                modifier = ColorIconSpecModifier
                    .clickable {
                        onColorPicked?.invoke(parsedColor)
                    },
            ) {
                Canvas(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize()
                        .align(Alignment.Center),
                    onDraw = {
                        drawCircle(color = Color(parsedColor))
                    },
                )
            }
        }
        item(span = {
            GridItemSpan(this.maxLineSpan)
        }) {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Preview
@Composable
private fun ColorSelectionPreview() {
    ExpenseManagerTheme {
        ColorSelectionScreen()
    }
}
