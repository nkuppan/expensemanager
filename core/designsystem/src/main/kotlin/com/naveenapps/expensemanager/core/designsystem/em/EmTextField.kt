package com.naveenapps.expensemanager.core.designsystem.em

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme

/**
 * The one free-text field: outlined, sm corners, label, optional placeholder, leading icon,
 * trailing slot and helper / error text. [selectAllOnFocus] selects the whole value when the
 * field gains focus, so a pre-filled value (an amount, a name) can be replaced by typing.
 */
@Composable
fun EmTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    selectAllOnFocus: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
) {
    var fieldValue by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    if (fieldValue.text != value) {
        // Changed from outside (calculator, restored state): keep the caret valid.
        fieldValue = fieldValue.copy(text = value, selection = TextRange(value.length))
    }
    var isFocused by remember { mutableStateOf(false) }
    // Select everything once per focus gain, after the frame, so the tap's own caret placement
    // has already happened and the select-all wins.
    LaunchedEffect(isFocused, selectAllOnFocus) {
        if (isFocused && selectAllOnFocus) {
            fieldValue = fieldValue.copy(selection = TextRange(0, fieldValue.text.length))
        }
    }
    OutlinedTextField(
        value = fieldValue,
        onValueChange = {
            fieldValue = it
            if (it.text != value) onValueChange(it.text)
        },
        modifier = modifier.onFocusChanged { isFocused = it.isFocused },
        enabled = enabled,
        readOnly = readOnly,
        textStyle = EmTheme.type.itemTitle,
        label = label?.let { { Text(text = it, maxLines = 1, overflow = TextOverflow.Ellipsis) } },
        placeholder = placeholder?.let { { Text(text = it, style = EmTheme.type.body) } },
        leadingIcon = leadingIcon?.let { { EmIcon(icon = it, contentDescription = null, size = EmTheme.icons.md) } },
        trailingIcon = trailing,
        supportingText = supportingText?.let { { Text(text = it, style = EmTheme.type.supporting) } },
        isError = isError,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
        shape = EmTheme.shapes.sm,
    )
}

/**
 * An amount: decimal keyboard, whole value selected on focus, [errorMessage] shown while
 * [isError]. [trailing] usually holds the calculator button.
 */
@Composable
fun EmAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    imeAction: ImeAction = ImeAction.Next,
) {
    EmTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        trailing = trailing,
        supportingText = errorMessage?.takeIf { isError && it.isNotEmpty() },
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = imeAction),
        selectAllOnFocus = true,
    )
}

/**
 * A read-only field that opens a picker (date, time, account) when tapped. Never shows the
 * keyboard.
 */
@Composable
fun EmPickerField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current
    LaunchedEffect(isPressed, isFocused) {
        if (isPressed || isFocused) {
            onClick()
            focusManager.clearFocus(force = true)
        }
    }
    EmTextField(
        value = value,
        onValueChange = {},
        modifier = modifier.pointerInput(onClick) { detectTapGestures(onTap = { onClick() }) },
        label = label,
        leadingIcon = leadingIcon,
        readOnly = true,
        interactionSource = interactionSource,
    )
}

/**
 * Search box for lists and sheets: filled, no underline, search icon, a clear button while
 * there is a query and an optional [trailingAction] after it.
 */
@Composable
fun EmSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingAction: (@Composable () -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    val container = EmTheme.colors.scheme.surfaceContainerHigh
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = EmTheme.spacing.xxxl),
        singleLine = true,
        shape = EmTheme.shapes.md,
        textStyle = EmTheme.type.itemTitle,
        placeholder = { Text(text = placeholder, style = EmTheme.type.body, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = { EmIcon(icon = Icons.Rounded.Search, contentDescription = null) },
        trailingIcon = if (query.isNotEmpty() || trailingAction != null) {
            {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (query.isNotEmpty()) {
                        EmIconButton(
                            icon = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.a11y_clear_search),
                            onClick = { onQueryChange("") },
                        )
                    }
                    trailingAction?.invoke()
                }
            }
        } else {
            null
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = container,
            unfocusedContainerColor = container,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
    )
}

/** Shown in place of a filtered list when nothing matches the search. */
@Composable
fun EmNoSearchResults(modifier: Modifier = Modifier) {
    EmText(
        text = stringResource(R.string.no_search_results),
        color = EmTextColor.Supporting,
        modifier = modifier,
    )
}
