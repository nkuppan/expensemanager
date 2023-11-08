package com.naveenapps.expensemanager.presentation.selection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.ui.components.RoundIconView
import com.naveenapps.expensemanager.ui.extensions.getDrawable
import com.naveenapps.expensemanager.ui.extensions.toColor
import com.naveenapps.expensemanager.ui.theme.ExpenseManagerTheme


@Composable
fun IconAndColorComponent(
    selectedColor: String,
    selectedIcon: String,
    openColorPicker: (() -> Unit)?,
    openIconPicker: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Row(modifier = modifier) {

        val containerColor = selectedColor.toColor()
        val contentColor = colorResource(id = R.color.white)

        RoundIconView(
            modifier = Modifier
                .size(40.dp)
                .clickable {
                    openColorPicker?.invoke()
                    focusManager.clearFocus(force = true)
                }
                .align(Alignment.CenterVertically),
            iconBackgroundColor = selectedColor
        )

        Text(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(8.dp),
            text = "+",
            fontSize = 24.sp
        )

        FilledTonalIconButton(
            modifier = Modifier.wrapContentSize(),
            onClick = {
                //Open icon selector
                openIconPicker?.invoke()
                focusManager.clearFocus(force = true)
            }) {
            Icon(
                painter = painterResource(
                    context.getDrawable(selectedIcon)
                ), contentDescription = ""
            )
        }

        Text(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(8.dp),
            text = "=",
            fontSize = 24.sp
        )

        FilledTonalIconButton(
            modifier = Modifier.wrapContentSize(),
            onClick = { },
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = containerColor,
                contentColor = contentColor
            )
        ) {
            Icon(
                painter = painterResource(
                    context.getDrawable(selectedIcon)
                ),
                contentDescription = ""
            )
        }
    }
}

@Composable
@Preview
private fun IconAndColorComponentPreview() {
    ExpenseManagerTheme {
        IconAndColorComponent(
            selectedColor = "#000000",
            selectedIcon = "ic_add",
            openColorPicker = {},
            openIconPicker = {}
        )
    }
}