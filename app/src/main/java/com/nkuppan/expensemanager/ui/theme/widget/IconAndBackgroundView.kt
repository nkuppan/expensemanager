package com.nkuppan.expensemanager.ui.theme.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nkuppan.expensemanager.ui.extensions.getDrawable
import com.nkuppan.expensemanager.ui.extensions.toColor
import com.nkuppan.expensemanager.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.ui.utils.IconSpecModifier
import com.nkuppan.expensemanager.ui.utils.SmallIconSpecModifier


@Composable
fun IconAndBackgroundView(
    icon: String,
    iconBackgroundColor: String,
    modifier: Modifier = Modifier,
    name: String? = null
) {
    IconView(
        modifier.then(IconSpecModifier),
        iconBackgroundColor,
        icon,
        name,
        18.dp
    )
}

@Composable
fun SmallIconAndBackgroundView(
    icon: String,
    iconBackgroundColor: String,
    modifier: Modifier = Modifier,
    name: String? = null
) {
    IconView(
        modifier.then(SmallIconSpecModifier),
        iconBackgroundColor, icon, name, 12.dp,
    )
}

@Composable
private fun IconView(
    modifier: Modifier,
    iconBackgroundColor: String,
    icon: String,
    name: String?,
    iconSize: Dp = 18.dp
) {
    val context = LocalContext.current

    Box(modifier = modifier) {
        RoundIconView(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            iconBackgroundColor = iconBackgroundColor
        )
        Image(
            modifier = Modifier
                .size(iconSize)
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
        Column {
            IconAndBackgroundView(
                icon = "ic_calendar",
                iconBackgroundColor = "#000000",
            )
            SmallIconAndBackgroundView(
                icon = "ic_calendar",
                iconBackgroundColor = "#000000",
            )
        }
    }
}