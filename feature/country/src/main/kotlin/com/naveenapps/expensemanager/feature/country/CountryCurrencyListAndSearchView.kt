package com.naveenapps.expensemanager.feature.country

import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.Country
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFieldValue

@Composable
internal fun CountryCurrencyListAndSearchView(
    viewModel: CountryListViewModel,
    onEvent: (CountrySelectionEvent) -> Unit,
    modifier: Modifier = Modifier
) {

    val countryState by viewModel.countryState.collectAsState()

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
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CountrySearchView(
                state = countryState,
                onAction = onAction
            )
        },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Results count
            if (countryState.searchText.value.isNotBlank()) {
                Text(
                    text = "${countryState.countries.size} results found",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            CountryCurrencyListView(
                countries = countryState.countries,
                selection = {
                    onAction.invoke(CountrySelectionAction.SelectCountry(it))
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CountrySearchView(
    state: CountryState,
    onAction: (CountrySelectionAction) -> Unit
) {
    TopAppBar(
        title = {
            OutlinedTextField(
                value = state.searchText.value,
                onValueChange = {
                    state.searchText.onValueChange?.invoke(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.search_country),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    if (state.showClearButton) {
                        IconButton(
                            onClick = {
                                onAction.invoke(CountrySelectionAction.ClearText)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    onAction.invoke(CountrySelectionAction.ClosePage)
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
internal fun CountryCurrencyListView(
    countries: List<Country>,
    selection: (Country) -> Unit,
    modifier: Modifier = Modifier
) {
    if (countries.isEmpty()) {
        // Empty state
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "No countries found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(countries, key = { it.name }) { country ->
                CountryWithCurrencyItemView(
                    country = country,
                    onClick = { selection(country) }
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
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag/Country icon container
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // If you have flag emojis or images, use them here
                    // Otherwise, show country code
                    Text(
                        text = country.name.take(2), // First 2 letters of country code
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Country and currency info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = country.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = country.currency.symbol,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = country.currency.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Arrow icon
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Composable
@AppPreviewsLightAndDarkMode
private fun CountryDetailsPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        CountryCurrencyListAndSearchView(
            countryState = CountryState(
                countries = listOf(
                    Country(
                        name = "India",
                        countryCode = "in",
                        currencyCode = "in",
                        currency = Currency(
                            name = "Rupees",
                            symbol = "₹"
                        )
                    ),
                    Country(
                        name = "USA",
                        countryCode = "us",
                        currencyCode = "us",
                        currency = Currency(
                            name = "Rupees", symbol = "$"
                        )
                    ),
                ),
                searchText = TextFieldValue("", false, onValueChange = {}),
                showClearButton = true
            ),
            onAction = {

            }
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