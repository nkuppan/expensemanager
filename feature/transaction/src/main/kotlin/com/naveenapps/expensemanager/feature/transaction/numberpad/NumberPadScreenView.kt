package com.naveenapps.expensemanager.feature.transaction.numberpad

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmCardVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.feature.transaction.R
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NumberPadDialogView(
    onConfirm: ((String?) -> Unit),
    initialAmount: Double? = null,
    header: (@Composable () -> Unit)? = null,
    viewModel: NumberPadViewModel = koinViewModel(),
) {
    // The keypad's ViewModel outlives the dialog, so seed it on every open: editing starts
    // from the transaction's amount instead of a blank (or a previous session's) value.
    LaunchedEffect(Unit) { viewModel.setInitialAmount(initialAmount) }

    val calculatedAmount by viewModel.calculatedAmount.collectAsStateWithLifecycle()
    val calculatedAmountString by viewModel.calculatedAmountString.collectAsStateWithLifecycle()

    NumberPadDialog(
        onConfirm = onConfirm,
        onChange = viewModel::appendString,
        calculatedAmount = calculatedAmount,
        calculatedAmountString = calculatedAmountString,
        clear = viewModel::clearAmount,
        header = header,
    )
}

@Composable
private fun NumberPadDialog(
    onConfirm: (String?) -> Unit,
    onChange: (String) -> Unit,
    clear: () -> Unit,
    calculatedAmount: String,
    calculatedAmountString: String,
    header: (@Composable () -> Unit)? = null,
) {
    Dialog(
        onDismissRequest = {
            onConfirm.invoke(null)
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        EmCard(
            modifier = Modifier
                .padding(EmTheme.spacing.lg)
                .wrapContentSize(),
            variant = EmCardVariant.Filled,
        ) {
            Column {
                // Optional content above the keypad, e.g. the add screen's frequent-entry chips.
                header?.invoke()
                NumberPadScreenView(
                    modifier = Modifier.wrapContentHeight(),
                    value = calculatedAmount,
                    amountString = calculatedAmountString,
                    onChange = {
                        onChange.invoke(it)
                    },
                    confirm = {
                        onConfirm.invoke(calculatedAmount)
                    },
                    cancel = {
                        // Cancel must keep the existing amount: null dismisses the pad without
                        // changing the value (same as tapping outside the dialog).
                        onConfirm.invoke(null)
                    },
                    clear = {
                        clear.invoke()
                    },
                )
            }
        }
    }
}

@Composable
private fun NumberPadScreenView(
    modifier: Modifier = Modifier,
    value: String = "0.0",
    amountString: String = "",
    onChange: ((String) -> Unit),
    confirm: (() -> Unit),
    cancel: (() -> Unit),
    clear: (() -> Unit),
) {
    Column(modifier = modifier) {
        // Display area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.md)
                .clip(EmTheme.shapes.lg)
                .background(EmTheme.colors.scheme.surfaceContainerHigh),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = EmTheme.spacing.xl, vertical = EmTheme.spacing.lg),
                horizontalAlignment = Alignment.End,
            ) {
                // Expression
                if (amountString.isNotBlank()) {
                    EmText(
                        text = amountString,
                        modifier = Modifier.fillMaxWidth(),
                        color = EmTextColor.Supporting,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(EmTheme.spacing.xs))
                }

                // Current value
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    EmText(
                        text = value,
                        modifier = Modifier.weight(1f, fill = false),
                        role = EmTextRole.Display,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.width(EmTheme.spacing.md))
                    EmIconButton(
                        icon = Icons.AutoMirrored.Rounded.Backspace,
                        contentDescription = stringResource(R.string.backspace),
                        onClick = { onChange.invoke("") },
                        modifier = Modifier.size(36.dp),
                        tint = EmTheme.colors.scheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(EmTheme.spacing.xs))

        // Number pad grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = EmTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xs),
        ) {
            val numberButtons = listOf(
                listOf("1", "2", "3", "/"),
                listOf("4", "5", "6", "×"),
                listOf("7", "8", "9", "−"),
                listOf(".", "0", "00", "+"),
            )

            val inputMap = mapOf(
                "×" to "*",
                "−" to "-",
            )

            numberButtons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.xs),
                ) {
                    row.forEach { label ->
                        val isOperator = label in listOf("/", "×", "−", "+")
                        val inputValue = inputMap[label] ?: label

                        NumberPadButton(
                            text = label,
                            isOperator = isOperator,
                            onClick = { onChange.invoke(inputValue) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(EmTheme.spacing.sm))

        // Bottom actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Cancel and Done share the width, and their labels stay on one line (shrinking if
            // needed): with a large font size Done used to be squeezed into a narrow column with
            // its label wrapping letter by letter.
            EmButton(
                text = stringResource(id = R.string.clear),
                onClick = { clear.invoke() },
                variant = EmButtonVariant.Tertiary,
            )
            EmButton(
                text = stringResource(id = R.string.cancel),
                onClick = { cancel.invoke() },
                variant = EmButtonVariant.Secondary,
                modifier = Modifier.weight(1f),
            )
            EmButton(
                text = stringResource(id = R.string.done),
                onClick = { confirm.invoke() },
                leadingIcon = Icons.Rounded.Check,
                modifier = Modifier.weight(1f),
            )
        }
    }
}


@Composable
private fun NumberPadButton(
    text: String,
    isOperator: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(EmTheme.shapes.md)
            .background(if (isOperator) EmTheme.colors.scheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        EmText(
            text = text,
            role = EmTextRole.ScreenTitle,
            color = if (isOperator) EmTextColor.Primary else EmTextColor.Default,
        )
    }
}

@EmPreviews
@Composable
fun NumberPadScreenPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        NumberPadScreenView(
            modifier = Modifier.wrapContentHeight(),
            value = "8,064",
            amountString = "5697+2367",
            {},
            {},
            {},
            {},
        )
    }
}

@EmPreviews
@Composable
fun NumberPadScreenDialogPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        NumberPadDialog(
            onConfirm = {},
            onChange = {},
            clear = {},
            calculatedAmount = "8,064",
            calculatedAmountString = "5697+2367",
        )
    }
}
