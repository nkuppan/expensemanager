package com.nkuppan.expensemanager.presentation.selection

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.theme.ExpenseManagerTheme

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
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 96.dp)
    ) {
        item(span = {
            GridItemSpan(4)
        }) {
            SelectionTitle(stringResource(id = R.string.choose_color))
        }
        items(colors) { color ->
            val parsedColor = android.graphics.Color.parseColor(color)
            Box(modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onColorPicked?.invoke(parsedColor)
                }
                .height(96.dp)) {
                Canvas(modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize()
                    .align(Alignment.Center),
                    onDraw = {
                        drawCircle(color = Color(parsedColor))
                    })
            }
        }
    }
}

@Composable
fun SelectionTitle(title: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        text = title,
        fontSize = 20.sp,
        fontStyle = FontStyle.Normal,
        fontWeight = FontWeight.SemiBold
    )
}

@Preview
@Composable
private fun ColorSelectionPreview() {
    ExpenseManagerTheme {
        ColorSelectionScreen()
    }
}