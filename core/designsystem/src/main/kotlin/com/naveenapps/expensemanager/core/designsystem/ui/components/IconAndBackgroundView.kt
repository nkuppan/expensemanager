package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.toColor
import com.naveenapps.expensemanager.core.designsystem.ui.utils.IconSpecModifier
import com.naveenapps.expensemanager.core.designsystem.ui.utils.SmallIconSpecModifier

@Composable
fun IconAndBackgroundView(
    icon: String,
    iconBackgroundColor: String,
    modifier: Modifier = Modifier,
    name: String? = null,
    customImagePath: String? = null,
) {
    IconView(
        modifier.then(IconSpecModifier),
        iconBackgroundColor,
        icon,
        name,
        18.dp,
        customImagePath,
    )
}

@Composable
fun SmallIconAndBackgroundView(
    icon: String,
    iconBackgroundColor: String,
    modifier: Modifier = Modifier,
    name: String? = null,
    iconSize: Dp = 12.dp,
    customImagePath: String? = null,
) {
    IconView(
        modifier.then(SmallIconSpecModifier),
        iconBackgroundColor = iconBackgroundColor,
        icon = icon,
        name = name,
        iconSize = iconSize,
        customImagePath = customImagePath,
    )
}

@Composable
private fun IconView(
    modifier: Modifier,
    iconBackgroundColor: String,
    icon: String,
    name: String?,
    iconSize: Dp = 18.dp,
    customImagePath: String? = null,
) {
    Box(modifier = modifier) {
        if (customImagePath == null) {
            RoundIconView(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                iconBackgroundColor = iconBackgroundColor,
            )
        }
        IconOrCustomImage(
            icon = icon,
            customImagePath = customImagePath,
            contentDescription = name,
            modifier = if (customImagePath != null) {
                Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            } else {
                Modifier
                    .size(iconSize)
                    .align(Alignment.Center)
            },
        )
    }
}

@Composable
fun RoundIconView(
    iconBackgroundColor: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            onDraw = {
                drawCircle(color = iconBackgroundColor.toColor())
            },
        )
    }
}

@Preview
@Composable
fun IconAndBackgroundViewPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        IconAndBackgroundView(
            icon = "account_balance_wallet",
            iconBackgroundColor = "#000000",
        )
        SmallIconAndBackgroundView(
            icon = "account_balance_wallet",
            iconBackgroundColor = "#000000",
            iconSize = 12.dp,
        )
    }
}
