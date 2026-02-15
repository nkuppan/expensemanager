package com.naveenapps.expensemanager.feature.currency

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingRow
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.CurrencyPosition
import com.naveenapps.expensemanager.core.model.toDisplayValue
import com.naveenapps.expensemanager.core.settings.domain.model.NumberFormatType
import com.naveenapps.expensemanager.feature.country.CountrySelectionEvent
import com.naveenapps.invoicebuilder.feature.country.CountryCurrencySelectionBottomSheet
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CurrencyCustomiseScreen(
    viewModel: CurrencyViewModel = koinViewModel(),
) {

    val state by viewModel.state.collectAsState()

    CurrencyScreen(
        state = state,
        onAction = viewModel::processAction,
    )
}

@Composable
private fun CurrencyScreen(
    state: CurrencyState,
    onAction: (CurrencyAction) -> Unit,
) {

    if (state.showCurrencySelection) {
        CountryCurrencySelectionBottomSheet(
            onEvent = { event ->
                when (event) {
                    CountrySelectionEvent.Dismiss -> {
                        onAction.invoke(CurrencyAction.DismissCurrencySelection)
                    }

                    is CountrySelectionEvent.CountrySelected -> {
                        onAction.invoke(
                            CurrencyAction.SelectCurrency(
                                event.country
                            )
                        )
                    }
                }
            }
        )
    }

    CurrencyCustomiseScreenContent(onAction, state)
}

@Composable
private fun CurrencyCustomiseScreenContent(
    onAction: (CurrencyAction) -> Unit,
    state: CurrencyState
) {
    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                title = stringResource(id = R.string.currency),
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = {
                    onAction.invoke(CurrencyAction.ClosePage)
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Currency Selection Section
            CurrencySection(
                title = "Selected Currency",
                subtitle = "Choose your preferred currency"
            ) {
                AppCardView(modifier = Modifier.fillMaxWidth()) {
                    SettingRow(
                        onClick = {
                            onAction.invoke(CurrencyAction.OpenCurrencySelection)
                        },
                        title = stringResource(id = R.string.select_currency),
                        value = state.currency.toDisplayValue(),
                        icon = Icons.Outlined.Paid,
                    )
                }
            }

            // Currency Position Section
            CurrencySection(
                title = stringResource(id = R.string.currency_position),
                subtitle = "Where the symbol appears"
            ) {
                CurrencyPositionSelector(
                    currency = state.currency.symbol,
                    selectedPosition = state.currency.position,
                    onPositionChange = {
                        onAction.invoke(CurrencyAction.ChangeCurrencyType(it))
                    }
                )
            }

            // Currency Format Section
            CurrencySection(
                title = stringResource(id = R.string.currency_format),
                subtitle = "Number formatting style"
            ) {
                CurrencyFormatSelector(
                    textFormat = state.numberFormatType,
                    onFormatChange = {
                        onAction.invoke(CurrencyAction.ChangeCurrencyNumberFormat(it))
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CurrencySection(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        content()
    }
}

@Composable
private fun CurrencyPositionSelector(
    currency: String,
    selectedPosition: CurrencyPosition,
    onPositionChange: (CurrencyPosition) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CurrencyPosition.entries.forEach { position ->
            val isSelected = position == selectedPosition
            val preview = when (position) {
                CurrencyPosition.PREFIX -> "${currency}1,234.56"
                CurrencyPosition.SUFFIX -> "1,234.56$currency"
            }

            Card(
                onClick = { onPositionChange(position) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                border = if (isSelected) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else {
                    null
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = position.name.replace("_", " ")
                                .lowercase()
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        Text(
                            text = preview,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFeatureSettings = "tnum"
                            ),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyFormatSelector(
    textFormat: NumberFormatType,
    onFormatChange: (NumberFormatType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NumberFormatType.entries.forEach { format ->
            val isSelected = format == textFormat
            val preview = when (format) {
                NumberFormatType.WITHOUT_ANY_SEPARATOR -> "123423.13"
                NumberFormatType.WITH_COMMA_SEPARATOR -> "1,23,423.13"
            }

            Card(
                onClick = { onFormatChange(format) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                border = if (isSelected) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else {
                    null
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = format.name.replace("_", " ")
                                .lowercase()
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        Text(
                            text = preview,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFeatureSettings = "tnum",
                                fontFamily = FontFamily.Monospace
                            ),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun CurrencyCustomiseScreenPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        CurrencyScreen(
            state = CurrencyState(
                showCurrencySelection = false,
                currency = Currency("$", "Dollar", code = "USD"),
                numberFormatType = NumberFormatType.WITHOUT_ANY_SEPARATOR
            ),
            onAction = {}
        )
    }
}
