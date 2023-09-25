package com.nkuppan.expensemanager.core.ui.theme.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nkuppan.expensemanager.core.ui.extensions.getDrawable


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
            .padding(end = 16.dp)
            .size(36.dp),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            onDraw = {
                drawCircle(
                    color = Color(
                        android.graphics.Color.parseColor(
                            iconBackgroundColor
                        )
                    )
                )
            }
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