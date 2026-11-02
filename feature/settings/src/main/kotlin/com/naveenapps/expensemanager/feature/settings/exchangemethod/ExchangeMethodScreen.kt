package com.naveenapps.expensemanager.feature.settings.exchangemethod

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.model.ExchangeMethod
import com.naveenapps.expensemanager.core.model.StaticExchangeRate
import com.naveenapps.expensemanager.feature.settings.R
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExchangeMethodScreen(
    viewModel: ExchangeMethodViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    ExchangeMethodScreenContent(
        state = state,
        onAction = viewModel::processAction,
    )
}

@Composable
private fun ExchangeMethodScreenContent(
    state: ExchangeMethodState,
    onAction: (ExchangeMethodAction) -> Unit,
) {
    state.editor?.let { editor ->
        StaticRateEditorDialog(
            editor = editor,
            currencyCodes = state.currencyCodes,
            onAction = onAction,
        )
    }

    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = { onAction.invoke(ExchangeMethodAction.ClosePage) },
                title = stringResource(R.string.exchange_method),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.exchange_method_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            AppCardView {
                MethodOption(
                    selected = state.method == ExchangeMethod.API,
                    title = stringResource(R.string.exchange_method_api),
                    description = stringResource(R.string.exchange_method_api_description),
                    onClick = { onAction(ExchangeMethodAction.SelectMethod(ExchangeMethod.API)) },
                )
                HorizontalDivider()
                MethodOption(
                    selected = state.method == ExchangeMethod.STATIC,
                    title = stringResource(R.string.exchange_method_static),
                    description = stringResource(R.string.exchange_method_static_description),
                    onClick = { onAction(ExchangeMethodAction.SelectMethod(ExchangeMethod.STATIC)) },
                )
                HorizontalDivider()
                MethodOption(
                    selected = state.method == ExchangeMethod.DISABLED,
                    title = stringResource(R.string.exchange_method_disabled),
                    description = stringResource(R.string.exchange_method_disabled_description),
                    onClick = {
                        onAction(ExchangeMethodAction.SelectMethod(ExchangeMethod.DISABLED))
                    },
                )
            }

            when (state.method) {
                ExchangeMethod.API -> ApiProviderCard(state.apiProvider, onAction)
                ExchangeMethod.STATIC -> StaticRatesCard(state.staticRates, onAction)
                ExchangeMethod.DISABLED -> Unit
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MethodOption(
    selected: Boolean,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(modifier = Modifier.padding(start = 4.dp, end = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ApiProviderCard(
    apiProvider: String?,
    onAction: (ExchangeMethodAction) -> Unit,
) {
    AppCardView {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = if (apiProvider != null) {
                    stringResource(R.string.exchange_method_provider, apiProvider)
                } else {
                    stringResource(R.string.exchange_method_no_provider)
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedButton(onClick = { onAction(ExchangeMethodAction.OpenApiProfile) }) {
                Text(stringResource(R.string.exchange_method_configure_provider))
            }
        }
    }
}

@Composable
private fun StaticRatesCard(
    rates: List<StaticExchangeRate>,
    onAction: (ExchangeMethodAction) -> Unit,
) {
    AppCardView {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            if (rates.isEmpty()) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    text = stringResource(R.string.exchange_method_no_pairs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            rates.forEach { rate ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAction(ExchangeMethodAction.EditStaticRate(rate)) }
                        .padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "1 ${rate.baseCode} → ${rate.quoteCode}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(
                                R.string.exchange_method_buy_sell,
                                rate.buy.toDisplayRate(),
                                rate.sell.toDisplayRate(),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { onAction(ExchangeMethodAction.DeleteStaticRate(rate)) }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.exchange_method_delete_pair),
                        )
                    }
                }
            }
            TextButton(
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = { onAction(ExchangeMethodAction.AddStaticRate) },
            ) {
                Text(stringResource(R.string.exchange_method_add_pair))
            }
        }
    }
}

@Composable
private fun StaticRateEditorDialog(
    editor: StaticRateEditorState,
    currencyCodes: List<String>,
    onAction: (ExchangeMethodAction) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onAction(ExchangeMethodAction.DismissEditor) },
        title = {
            Text(
                stringResource(
                    if (editor.original != null) {
                        R.string.exchange_method_edit_pair
                    } else {
                        R.string.exchange_method_add_pair
                    },
                ),
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CurrencyCodeField(
                    label = stringResource(R.string.exchange_method_base_currency),
                    code = editor.baseCode,
                    currencyCodes = currencyCodes,
                    onChange = { onAction(ExchangeMethodAction.ChangeEditorBase(it)) },
                )
                CurrencyCodeField(
                    label = stringResource(R.string.exchange_method_quote_currency),
                    code = editor.quoteCode,
                    currencyCodes = currencyCodes,
                    onChange = { onAction(ExchangeMethodAction.ChangeEditorQuote(it)) },
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = editor.buyText,
                    onValueChange = { onAction(ExchangeMethodAction.ChangeEditorBuy(it)) },
                    label = {
                        Text(
                            stringResource(
                                R.string.exchange_method_buy_label,
                                editor.baseCode,
                                editor.quoteCode,
                            ),
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = editor.sellText,
                    onValueChange = { onAction(ExchangeMethodAction.ChangeEditorSell(it)) },
                    label = {
                        Text(
                            stringResource(
                                R.string.exchange_method_sell_label,
                                editor.baseCode,
                                editor.quoteCode,
                            ),
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                editor.errorMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onAction(ExchangeMethodAction.SaveEditor) }) {
                Text(stringResource(R.string.exchange_method_save))
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(ExchangeMethodAction.DismissEditor) }) {
                Text(stringResource(R.string.exchange_method_cancel))
            }
        },
    )
}

/** A currency code: typed freely, or picked from the currencies the app already uses. */
@Composable
private fun CurrencyCodeField(
    label: String,
    code: String,
    currencyCodes: List<String>,
    onChange: (String) -> Unit,
) {
    Column {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = code,
            onValueChange = { onChange(it.take(3).uppercase()) },
            label = { Text(label) },
            singleLine = true,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            currencyCodes.forEach { option ->
                FilterChip(
                    selected = option == code,
                    onClick = { onChange(option) },
                    label = { Text(option) },
                )
            }
        }
    }
}

private fun Double.toDisplayRate(): String = if (this == toLong().toDouble()) toLong().toString() else toString()

@Preview
@Composable
private fun ExchangeMethodScreenPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        ExchangeMethodScreenContent(
            state = ExchangeMethodState(
                method = ExchangeMethod.STATIC,
                staticRates = listOf(StaticExchangeRate("USD", "SYP", 138.0, 140.0)),
                currencyCodes = listOf("SYP", "USD"),
            ),
            onAction = {},
        )
    }
}
