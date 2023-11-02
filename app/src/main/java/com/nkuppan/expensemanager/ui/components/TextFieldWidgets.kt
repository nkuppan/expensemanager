package com.nkuppan.expensemanager.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.nkuppan.expensemanager.ui.utils.UiText


@Composable
fun ClickableTextField(
    label: Int,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: Int? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed: Boolean by interactionSource.collectIsPressedAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isPressed) {
        if (isPressed) {
            onClick.invoke()
            focusManager.clearFocus(force = true)
        }
    }


    OutlinedTextField(
        modifier = modifier,
        interactionSource = interactionSource,
        value = value,
        singleLine = true,
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    painter = painterResource(id = leadingIcon),
                    contentDescription = ""
                )
            }
        } else {
            null
        },
        label = {
            Text(text = stringResource(id = label))
        },
        onValueChange = {}
    )
}

@Composable
fun StringTextField(
    value: String,
    errorMessage: UiText?,
    onValueChange: ((String) -> Unit)?,
    label: Int,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    singleLine: Boolean = true
) {

    val context = LocalContext.current

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
        isError = errorMessage != null,
        supportingText = if (errorMessage != null) {
            { Text(text = errorMessage.asString(context)) }
        } else {
            null
        }
    )
}

@Composable
fun DecimalTextField(
    value: String,
    errorMessage: UiText?,
    onValueChange: ((String) -> Unit)?,
    leadingIcon: Int?,
    label: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        modifier = modifier,
        value = value,
        singleLine = true,
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(painter = painterResource(id = leadingIcon), contentDescription = "")
            }
        } else {
            null
        },
        label = {
            Text(text = stringResource(id = label))
        },
        onValueChange = {
            onValueChange?.invoke(it)
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus(force = true)
            }
        ),
        isError = errorMessage != null,
        supportingText = if (errorMessage != null) {
            { Text(text = errorMessage.asString(context)) }
        } else {
            null
        }
    )
}