package com.naveenapps.expensemanager.feature.transaction.numberpad

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.feature.transaction.R
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NumberPadDialogView(
    onConfirm: ((String?) -> Unit),
    viewModel: NumberPadViewModel = koinViewModel()
) {
    val calculatedAmount by viewModel.calculatedAmount.collectAsState()
    val calculatedAmountString by viewModel.calculatedAmountString.collectAsState()

    NumberPadDialog(
        onConfirm = onConfirm,
        onChange = viewModel::appendString,
        calculatedAmount = calculatedAmount,
        calculatedAmountString = calculatedAmountString,
        clear = viewModel::clearAmount
    )
}

@Composable
private fun NumberPadDialog(
    onConfirm: (String?) -> Unit,
    onChange: (String) -> Unit,
    clear: () -> Unit,
    calculatedAmount: String,
    calculatedAmountString: String,
) {
    Dialog(
        onDismissRequest = {
            onConfirm.invoke(null)
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .wrapContentSize(),
            shape = RoundedCornerShape(8.dp),
        ) {
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
                    onConfirm.invoke("0")
                },
                clear = {
                    clear.invoke()
                },
            )
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
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.End,
            ) {
                // Expression
                if (amountString.isNotBlank()) {
                    Text(
                        text = amountString,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Current value
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                        textAlign = TextAlign.End,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = { onChange.invoke("") },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Backspace,
                            contentDescription = stringResource(R.string.backspace),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Number pad grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
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
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
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

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { clear.invoke() },
            ) {
                Text(
                    text = stringResource(id = R.string.clear),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = { cancel.invoke() },
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    fontWeight = FontWeight.Medium,
                )
            }

            Button(
                onClick = { confirm.invoke() },
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(id = R.string.done),
                    fontWeight = FontWeight.SemiBold,
                )
            }
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
    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = MaterialTheme.shapes.medium,
        color = if (isOperator)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else
            Color.Transparent,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = if (isOperator)
                    MaterialTheme.typography.titleLarge
                else
                    MaterialTheme.typography.titleMedium,
                fontWeight = if (isOperator) FontWeight.Bold else FontWeight.Medium,
                color = if (isOperator)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@AppPreviewsLightAndDarkMode
@Composable
fun NumberPadScreenPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        NumberPadScreenView(
            modifier = Modifier.wrapContentHeight(),
            value = "8,064",
            amountString = "5697+2367",
            {}, {}, {}, {},
        )
    }
}

@AppPreviewsLightAndDarkMode
@Composable
fun NumberPadScreenDialogPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        NumberPadDialog(
            onConfirm = {},
            onChange = {},
            clear = {},
            calculatedAmount = "8,064",
            calculatedAmountString = "5697+2367",
        )
    }
}
