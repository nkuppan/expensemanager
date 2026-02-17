package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.designsystem.R
import java.util.Date

@Composable
fun ClickableTextField(
    label: Int,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed: Boolean by interactionSource.collectIsPressedAsState()
    val isFocused: Boolean by interactionSource.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isPressed) {
        if (isPressed) {
            onClick.invoke()
            focusManager.clearFocus(force = true)
        }
    }

    LaunchedEffect(isFocused) {
        if (isFocused) {
            onClick.invoke()
            focusManager.clearFocus(force = true)
        }
    }

    val gesture = Modifier.pointerInput(onClick) {
        detectTapGestures(
            onPress = { _ ->
                onClick()
            },
            onTap = { _ ->
                onClick()
            }
        )
    }

    OutlinedTextField(
        modifier = modifier.then(gesture),
        interactionSource = interactionSource,
        value = value,
        singleLine = true,
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = "",
                )
            }
        } else {
            null
        },
        label = {
            Text(text = stringResource(id = label))
        },
        onValueChange = {},
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun StringTextField(
    value: String,
    isError: Boolean,
    onValueChange: ((String) -> Unit)?,
    label: Int,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    minLines: Int = 1,
    singleLine: Boolean = minLines == 1,
    errorMessage: String = "",
) {
    AutoSelectTextField(
        text = value,
        onTextChange = { onValueChange?.invoke(it) },
        label = {
            Text(
                text = stringResource(id = label),
                overflow = TextOverflow.Ellipsis,
                maxLines = if (singleLine) 1 else Int.MAX_VALUE,
            )
        },
        singleLine = singleLine,
        isError = isError,
        errorMessage = errorMessage,
        modifier = modifier,
        keyboardOptions = keyboardOptions
    )
}

@Composable
fun DecimalTextField(
    value: String,
    isError: Boolean,
    onValueChange: ((String) -> Unit)?,
    leadingIconText: String?,
    label: Int,
    modifier: Modifier = Modifier,
    errorMessage: String = "",
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    AutoSelectTextField(
        text = value,
        onTextChange = { onValueChange?.invoke(it) },
        label = {
            Text(
                text = stringResource(id = label),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        singleLine = true,
        isError = isError,
        errorMessage = errorMessage,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next,
        ),
        trailingIcon = trailingIcon
    )
}

@Composable
fun NumberTextField(
    value: String,
    isError: Boolean,
    onValueChange: ((String) -> Unit)?,
    label: Int,
    modifier: Modifier = Modifier,
    errorMessage: String = "",
    leadingIconText: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    AutoSelectTextField(
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon,
        text = value,
        onTextChange = { onValueChange?.invoke(it) },
        label = {
            Text(
                text = stringResource(id = label),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        placeholder = placeholder,
        supportingText = supportingText,
        singleLine = true,
        isError = isError,
        errorMessage = errorMessage,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
        )
    )
}

@Preview
@Composable
private fun TextFieldPreviews() {
    NaveenAppsPreviewTheme {
        Column {
            NumberTextField(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                value = "Sample",
                isError = true,
                onValueChange = {},
                label = R.string.choose_color,
                errorMessage = "Should not add the value",
            )
            DecimalTextField(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                value = "1.00",
                isError = true,
                onValueChange = { },
                leadingIconText = "$",
                label = R.string.choose_color,
                errorMessage = "Should not add the value",
            )
            StringTextField(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                value = "Sample",
                isError = true,
                onValueChange = { },
                label = R.string.choose_color,
                errorMessage = "Should not add the value",
            )
            ClickableTextField(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                value = Date().toCompleteDateWithDate(),
                label = R.string.choose_color,
                leadingIcon = Icons.Default.EditCalendar,
                onClick = {},
            )
        }
    }
}
