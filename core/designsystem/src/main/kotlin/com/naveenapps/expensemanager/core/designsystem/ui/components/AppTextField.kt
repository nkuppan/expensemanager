@file:OptIn(ExperimentalFoundationApi::class)

package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode

@Composable
fun AutoSelectTextField(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    errorMessage: String = "",
    isError: Boolean = false,
    minLines: Int = 1,
    placeholder: String? = null,
    supportingText: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
) {
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = text,
                selection = TextRange(0) // initially no selection
            )
        )
    }

    if (textFieldValue.text != text) {
        textFieldValue = textFieldValue.copy(
            text = text
        )
    }

    var hasFocusedOnce by remember { mutableStateOf(false) }

    AppTextField(
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon,
        value = textFieldValue,
        onValueChange = {
            // When text is updated externally (from outside), update internal state
            if (it.text != text) {
                onTextChange(it.text)
                textFieldValue = it.copy(text = it.text)
            }
        },
        placeholder = placeholder,
        supportingText = supportingText,
        label = label,
        singleLine = singleLine,
        modifier = modifier
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    // Select all only the first time the field is focused
                    if (!hasFocusedOnce) {
                        hasFocusedOnce = true
                        textFieldValue = textFieldValue.copy(
                            selection = TextRange(0, textFieldValue.text.length)
                        )
                    }
                } else {
                    hasFocusedOnce = false
                }
            },
        errorMessage = errorMessage,
        isError = isError,
        minLines = minLines,
        keyboardOptions = keyboardOptions
    )
}

@Composable
fun AppTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: @Composable (() -> Unit)?,
    singleLine: Boolean,
    modifier: Modifier = Modifier,
    errorMessage: String = "",
    isError: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = 1,
    placeholder: String? = null,
    supportingText: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            onValueChange.invoke(it)
        },
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon,
        modifier = modifier,
        label = label,
        singleLine = singleLine,
        shape = RoundedCornerShape(8.dp),
        isError = isError,
        supportingText = if (isError) {
            errorMessage.let {
                { Text(it) }
            }
        } else {
            supportingText?.let {
                { Text(it) }
            }
        },
        placeholder = placeholder?.let {
            { Text(it) }
        },
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
    )
}

@AppPreviewsLightAndDarkMode
@Composable
private fun AppTextFieldPreview() {
    NaveenAppsPreviewTheme {
        var text by remember { mutableStateOf("Editable text") }

        AutoSelectTextField(
            text = text,
            onTextChange = { text = it },
            label = { Text("Enter something") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}