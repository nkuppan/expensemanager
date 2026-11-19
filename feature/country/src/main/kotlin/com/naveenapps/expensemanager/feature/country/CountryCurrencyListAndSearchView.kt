package com.naveenapps.expensemanager.feature.country

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextField
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.Country
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFieldValue

@Composable
internal fun CountryCurrencyListAndSearchView(
    viewModel: CountryListViewModel,
    onEvent: (CountrySelectionEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val countryState by viewModel.countryState.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.event) {
        onEvent.invoke(it)
    }

    CountryCurrencyListAndSearchView(
        modifier = modifier,
        countryState = countryState,
        onAction = viewModel::processAction,
    )
}

@Composable
internal fun CountryCurrencyListAndSearchView(
    countryState: CountryState,
    onAction: ((CountrySelectionAction) -> Unit),
    modifier: Modifier = Modifier,
) {
    EmScaffold(
        topBar = {
            CountrySearchView(
                state = countryState,
                onAction = onAction,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Results count
            if (countryState.searchText.value.isNotBlank()) {
                EmText(
                    text = "${countryState.countries.size} results found",
                    modifier = Modifier.padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.sm),
                    role = EmTextRole.Supporting,
                    color = EmTextColor.Supporting,
                )
            }

            CountryCurrencyListView(
                countries = countryState.countries,
                selection = {
                    onAction.invoke(CountrySelectionAction.SelectCountry(it))
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
internal fun CountrySearchView(
    state: CountryState,
    onAction: (CountrySelectionAction) -> Unit,
) {
    EmTopBar(
        title = null,
        navigation = EmTopBarNavigation.Back,
        onNavigationClick = { onAction.invoke(CountrySelectionAction.ClosePage) },
        titleContent = {
            EmTextField(
                value = state.searchText.value,
                onValueChange = {
                    state.searchText.onValueChange?.invoke(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = stringResource(id = R.string.search_country),
                leadingIcon = Icons.Outlined.Search,
                trailing = {
                    if (state.showClearButton) {
                        EmIconButton(
                            icon = Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.clear_search_text),
                            onClick = {
                                onAction.invoke(CountrySelectionAction.ClearText)
                            },
                        )
                    }
                },
                singleLine = true,
            )
        },
    )
}

@Composable
internal fun CountryCurrencyListView(
    countries: List<Country>,
    selection: (Country) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (countries.isEmpty()) {
        // Empty state
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
            ) {
                EmIcon(
                    icon = Icons.Outlined.SearchOff,
                    contentDescription = null,
                    size = EmTheme.icons.hero,
                    tint = EmTheme.colors.scheme.onSurfaceVariant,
                )
                EmText(
                    text = stringResource(id = R.string.no_countries_found),
                    color = EmTextColor.Supporting,
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(vertical = EmTheme.spacing.sm),
        ) {
            items(countries, key = { it.name }) { country ->
                CountryWithCurrencyItemView(
                    country = country,
                    onClick = { selection(country) },
                )
            }
        }
    }
}

@Composable
internal fun CountryWithCurrencyItemView(
    country: Country,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Flag/Country icon container
            Box(
                modifier = Modifier
                    .size(EmTheme.icons.tile)
                    .clip(EmTheme.shapes.md)
                    .background(EmTheme.colors.scheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                // First two letters of the country name
                EmText(
                    text = country.name.take(2),
                    role = EmTextRole.SectionTitle,
                    color = EmTheme.colors.scheme.onPrimaryContainer,
                )
            }

            // Country and currency info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xs),
            ) {
                EmText(
                    text = country.name,
                    role = EmTextRole.ItemTitle,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EmText(
                        text = country.currency.symbol,
                        role = EmTextRole.Label,
                        color = EmTextColor.Primary,
                    )
                    EmText(
                        text = "•",
                        role = EmTextRole.Supporting,
                        color = EmTextColor.Supporting,
                    )
                    EmText(
                        text = country.currency.name,
                        color = EmTextColor.Supporting,
                    )
                }
            }

            // Arrow icon
            EmIcon(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = EmTheme.colors.scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
@EmPreviews
private fun CountryDetailsPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        CountryCurrencyListAndSearchView(
            countryState = CountryState(
                countries = listOf(
                    Country(
                        name = "India",
                        countryCode = "in",
                        currencyCode = "in",
                        currency = Currency(
                            name = "Rupees",
                            symbol = "₹",
                        ),
                    ),
                    Country(
                        name = "USA",
                        countryCode = "us",
                        currencyCode = "us",
                        currency = Currency(
                            name = "Rupees",
                            symbol = "$",
                        ),
                    ),
                ),
                searchText = TextFieldValue("", false, onValueChange = {}),
                showClearButton = true,
            ),
            onAction = {
            },
        )
    }
}

fun Country.getCurrencyName(context: Context): String {
    val currency = this.currency

    val currencyName = currency.name.ifBlank {
        currency.namePlural.ifBlank {
            this.name
        }
    }

    val currencySymbol = currency.symbol.ifBlank {
        currency.nativeSymbol.ifBlank {
            this.countryCode
        }
    }

    return context.getString(R.string.currency_description, currencyName, currencySymbol)
}
