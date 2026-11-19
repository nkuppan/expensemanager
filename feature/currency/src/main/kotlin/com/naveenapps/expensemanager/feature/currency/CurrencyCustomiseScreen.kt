package com.naveenapps.expensemanager.feature.currency

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountSize
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountText
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmCardVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSettingItem
import com.naveenapps.expensemanager.core.designsystem.em.EmSettingTrailing
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.CurrencyPosition
import com.naveenapps.expensemanager.core.model.toDisplayValue
import com.naveenapps.expensemanager.core.settings.domain.model.NumberFormatType
import com.naveenapps.expensemanager.feature.country.CountryCurrencySelectionBottomSheet
import com.naveenapps.expensemanager.feature.country.CountrySelectionEvent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CurrencyCustomiseScreen(
    viewModel: CurrencyViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
                                event.country,
                            ),
                        )
                    }
                }
            },
        )
    }

    CurrencyCustomiseScreenContent(onAction, state)
}

@Composable
private fun CurrencyCustomiseScreenContent(
    onAction: (CurrencyAction) -> Unit,
    state: CurrencyState,
) {
    EmScaffold(
        topBar = {
            EmTopBar(
                title = stringResource(id = R.string.currency),
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = {
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
            verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xl),
        ) {
            // Currency Selection Section
            CurrencySection(
                title = stringResource(id = R.string.selected_currency),
                subtitle = stringResource(id = R.string.choose_preferred_currency),
            ) {
                EmCard(modifier = Modifier.fillMaxWidth()) {
                    EmSettingItem(
                        icon = Icons.Outlined.Paid,
                        title = stringResource(id = R.string.select_currency),
                        trailing = EmSettingTrailing.valueOrChevron(state.currency.toDisplayValue()),
                        onClick = {
                            onAction.invoke(CurrencyAction.OpenCurrencySelection)
                        },
                    )
                }
            }

            // Currency Position Section
            CurrencySection(
                title = stringResource(id = R.string.currency_position),
                subtitle = stringResource(id = R.string.where_symbol_appears),
            ) {
                CurrencyPositionSelector(
                    currency = state.currency.symbol,
                    selectedPosition = state.currency.position,
                    onPositionChange = {
                        onAction.invoke(CurrencyAction.ChangeCurrencyType(it))
                    },
                )
            }

            // Currency Format Section
            CurrencySection(
                title = stringResource(id = R.string.currency_format),
                subtitle = stringResource(id = R.string.number_formatting_style),
            ) {
                CurrencyFormatSelector(
                    textFormat = state.numberFormatType,
                    onFormatChange = {
                        onAction.invoke(CurrencyAction.ChangeCurrencyNumberFormat(it))
                    },
                )
            }

            Spacer(modifier = Modifier.height(EmTheme.spacing.lg))
        }
    }
}

@Composable
private fun CurrencySection(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = EmTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.md),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xs)) {
            EmText(
                text = title,
                role = EmTextRole.SectionTitle,
            )
            EmText(
                text = subtitle,
                role = EmTextRole.Supporting,
                color = EmTextColor.Supporting,
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
    ) {
        CurrencyPosition.entries.forEach { position ->
            val isSelected = position == selectedPosition
            val preview = when (position) {
                CurrencyPosition.PREFIX -> "${currency}1,234.56"
                CurrencyPosition.SUFFIX -> "1,234.56$currency"
            }

            EmCard(
                onClick = { onPositionChange(position) },
                modifier = Modifier.fillMaxWidth(),
                variant = if (isSelected) EmCardVariant.Highlight else EmCardVariant.Filled,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(EmTheme.spacing.lg),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xs),
                    ) {
                        EmText(
                            text = position.name.replace("_", " ")
                                .lowercase()
                                .replaceFirstChar { it.uppercase() },
                            role = if (isSelected) EmTextRole.ItemTitle else EmTextRole.Body,
                            color = if (isSelected) {
                                EmTheme.colors.scheme.onPrimaryContainer
                            } else {
                                EmTheme.colors.scheme.onSurface
                            },
                        )
                        EmAmountText(amount = preview, size = EmAmountSize.Small)
                    }

                    if (isSelected) {
                        EmIcon(
                            icon = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmTheme.colors.scheme.primary,
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
    ) {
        NumberFormatType.entries.forEach { format ->
            val isSelected = format == textFormat
            val preview = when (format) {
                NumberFormatType.WITHOUT_ANY_SEPARATOR -> "123423.13"
                NumberFormatType.WITH_COMMA_SEPARATOR -> "123,423.13"
            }

            EmCard(
                onClick = { onFormatChange(format) },
                modifier = Modifier.fillMaxWidth(),
                variant = if (isSelected) EmCardVariant.Highlight else EmCardVariant.Filled,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(EmTheme.spacing.lg),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xs),
                    ) {
                        EmText(
                            text = format.name.replace("_", " ")
                                .lowercase()
                                .replaceFirstChar { it.uppercase() },
                            role = if (isSelected) EmTextRole.ItemTitle else EmTextRole.Body,
                            color = if (isSelected) {
                                EmTheme.colors.scheme.onPrimaryContainer
                            } else {
                                EmTheme.colors.scheme.onSurface
                            },
                        )
                        EmText(text = preview, role = EmTextRole.Code)
                    }

                    if (isSelected) {
                        EmIcon(
                            icon = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmTheme.colors.scheme.primary,
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
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        CurrencyScreen(
            state = CurrencyState(
                showCurrencySelection = false,
                currency = Currency("$", "Dollar", code = "USD"),
                numberFormatType = NumberFormatType.WITHOUT_ANY_SEPARATOR,
            ),
            onAction = {},
        )
    }
}
