package com.nkuppan.expensemanager.presentation.selection

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.extensions.getDrawable
import com.nkuppan.expensemanager.core.ui.theme.ExpenseManagerTheme


@Composable
fun IconAndColorComponent(
    selectedColor: String,
    selectedIcon: String,
    openColorPicker: (() -> Unit)?,
    openIconPicker: (() -> Unit)?
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
    ) {

        val containerColor = Color(android.graphics.Color.parseColor(selectedColor))
        val contentColor = colorResource(id = R.color.white)

        FilledTonalButton(
            modifier = Modifier.wrapContentSize(), onClick = {
                openColorPicker?.invoke()
                focusManager.clearFocus(force = true)
            }, colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = containerColor,
                contentColor = contentColor
            ), shape = ButtonDefaults.shape
        ) {
            Text(text = stringResource(id = R.string.select_color))
        }

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