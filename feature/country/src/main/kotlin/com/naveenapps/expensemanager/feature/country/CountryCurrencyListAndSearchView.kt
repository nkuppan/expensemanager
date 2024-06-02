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
import com.naveenapps.expensemanager.core.designsystem.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.model.Country
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFieldValue

@Composable
internal fun CountryCurrencyListAndSearchView(
    modifier: Modifier = Modifier,
    countryListViewModel: CountryListViewModel,
    selection: ((Country?) -> Unit)?
) {

    val countryState by countryListViewModel.countryState.collectAsState()

    CountryCurrencyListAndSearchView(
        modifier = modifier,
        countryState = countryState,
        selection = {
            selection?.invoke(it)
            countryState.searchText.onValueChange?.invoke("")
        },
    )
}

@Composable
internal fun CountryCurrencyListAndSearchView(
    countryState: CountryState,
    selection: ((Country?) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CountrySearchView(
                searchText = countryState.searchText,
                dismiss = {
                    selection?.invoke(null)
                }
            )
        },
    ) {
        CountryCurrencyListView(
            countries = countryState.countries,
            selection = selection,
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
    ExpenseManagerTheme {
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
                searchText = TextFieldValue("", false, onValueChange = {})
            ),
            selection = {

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