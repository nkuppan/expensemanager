package com.nkuppan.expensemanager.common.ui.theme.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nkuppan.expensemanager.common.ui.extensions.getDrawable
import com.nkuppan.expensemanager.common.ui.extensions.toColor
import com.nkuppan.expensemanager.common.ui.theme.ExpenseManagerTheme


@Composable
fun IconAndBackgroundView(
    icon: String,
    iconBackgroundColor: String,
    modifier: Modifier = Modifier,
    name: String? = null
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .size(36.dp),
    ) {
        RoundIconView(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            iconBackgroundColor = iconBackgroundColor
        )
        Image(
            modifier = Modifier
                .size(18.dp)
                .align(Alignment.Center),
            painter = painterResource(id = context.getDrawable(icon)),
            colorFilter = ColorFilter.tint(color = Color.White),
            contentDescription = name
        )
    }
}

@Composable
fun RoundIconView(
    iconBackgroundColor: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            onDraw = {
                drawCircle(color = iconBackgroundColor.toColor())
            }
        )
    }
}

@Preview
@Composable
fun IconAndBackgroundViewPreview() {
    ExpenseManagerTheme {
        IconAndBackgroundView(
            icon = "ic_calendar",
            iconBackgroundColor = "#000000",
        )
    }
}