package com.naveenapps.expensemanager.core.designsystem.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naveenapps.expensemanager.core.designsystem.ui.components.RoundIconView
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.getDrawable
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.toColor
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import kotlinx.coroutines.launch

enum class SelectionType {
    NONE,
    COLOR_SELECTION,
    ICON_SELECTION,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconAndColorComponent(
    selectedColor: String,
    selectedIcon: String,
    onColorSelection: ((Int) -> Unit)?,
    onIconSelection: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = {
            Log.d("******", "IconAndColorComponent: $it")
            true
        },
    )

    val scope = rememberCoroutineScope()
    var sheetSelection by remember { mutableStateOf(SelectionType.NONE) }

    if (sheetSelection != SelectionType.NONE) {
        ModalBottomSheet(
            modifier = Modifier.wrapContentSize(),
            sheetState = sheetState,
            onDismissRequest = {
                sheetSelection = SelectionType.NONE
            },
            windowInsets = WindowInsets(0.dp),
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 0.dp,
        ) {
            when (sheetSelection) {
                SelectionType.COLOR_SELECTION -> {
                    ColorSelectionScreen {
                        onColorSelection?.invoke(it)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                sheetSelection = SelectionType.NONE
                            }
                        }
                    }
                }

                SelectionType.ICON_SELECTION -> {
                    IconSelectionScreen {
                        onIconSelection?.invoke(context.resources.getResourceName(it))
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                sheetSelection = SelectionType.NONE
                            }
                        }
                    }
                }

                else -> Unit
            }
        }
    }

    Row(modifier = modifier) {
        val containerColor = selectedColor.toColor()
        val contentColor = colorResource(id = android.R.color.white)

        RoundIconView(
            modifier = Modifier
                .size(40.dp)
                .clickable {
                    sheetSelection = SelectionType.COLOR_SELECTION
                    focusManager.clearFocus(force = true)
                }
                .align(Alignment.CenterVertically),
            iconBackgroundColor = selectedColor,
        )

        Text(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(8.dp),
            text = "+",
            fontSize = 24.sp,
        )

        FilledTonalIconButton(
            modifier = Modifier.wrapContentSize(),
            onClick = {
                sheetSelection = SelectionType.ICON_SELECTION
                focusManager.clearFocus(force = true)
            },
        ) {
            Icon(
                painter = painterResource(
                    context.getDrawable(selectedIcon),
                ),
                contentDescription = "",
            )
        }

        Text(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(8.dp),
            text = "=",
            fontSize = 24.sp,
        )

        FilledTonalIconButton(
            modifier = Modifier.wrapContentSize(),
            onClick = { },
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
        ) {
            Icon(
                painter = painterResource(
                    context.getDrawable(selectedIcon),
                ),
                contentDescription = "",
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
            onColorSelection = {},
            onIconSelection = {},
        )
    }
}
