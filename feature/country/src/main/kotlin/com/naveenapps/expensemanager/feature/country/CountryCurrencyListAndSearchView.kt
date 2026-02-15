package com.naveenapps.expensemanager.feature.country

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.Country
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.feature.country.components.CountrySearchView
import com.naveenapps.expensemanager.feature.country.components.CountryWithCurrencyItemView

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
    ) {
        CountryCurrencyListView(
            countries = countryState.countries,
            selection = {
                onAction.invoke(CountrySelectionAction.SelectCountry(it))
            },
            modifier = modifier
                .fillMaxSize()
                .padding(it)
        )
    }
}

@Composable
internal fun CountryCurrencyListView(
    countries: List<Country>,
    selection: ((Country) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(modifier = modifier) {
        items(countries) {
            CountryWithCurrencyItemView(
                name = it.name,
                description = it.getCurrencyName(context),
                modifier = Modifier
                    .clickable {
                        selection?.invoke(it)
                    }
                    .padding(16.dp)
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

    currency ?: return this.currencyCode

    val currencyName = if (currency.name.isNotBlank()) {
        currency.name
    } else if (currency.namePlural.isNotBlank()) {
        currency.namePlural
    } else {
        this.name
    }

    val currencySymbol = if (currency.symbol.isNotBlank()) {
        currency.symbol
    } else if (currency.nativeSymbol.isNotBlank()) {
        currency.nativeSymbol
    } else {
        this.countryCode
    }

    return context.getString(R.string.currency_description, currencyName, currencySymbol)
}