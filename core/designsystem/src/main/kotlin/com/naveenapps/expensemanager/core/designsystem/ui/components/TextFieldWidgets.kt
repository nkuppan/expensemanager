package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

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
    singleLine: Boolean = true,
    errorMessage: String = "",
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        singleLine = singleLine,
        label = {
            Text(text = stringResource(id = label))
        },
        onValueChange = {
            onValueChange?.invoke(it)
        },
        keyboardOptions = keyboardOptions,
        isError = isError,
        supportingText = if (isError) {
            { Text(text = errorMessage) }
        } else {
            null
        },
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
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        modifier = modifier,
        value = value,
        singleLine = true,
        leadingIcon = if (leadingIconText != null) {
            {
                Text(
                    text = leadingIconText,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        } else {
            null
        },
        trailingIcon = trailingIcon,
        label = {
            Text(text = stringResource(id = label))
        },
        onValueChange = {
            onValueChange?.invoke(it)
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus(force = true)
            },
        ),
        isError = isError,
        supportingText = if (isError) {
            { Text(text = errorMessage) }
        } else {
            null
        },
    )
}
