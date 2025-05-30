package com.naveenapps.expensemanager.feature.country

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CountryCurrencySelectionDialog(
    modifier: Modifier = Modifier,
    onEvent: (CountrySelectionEvent) -> Unit,
    countryListViewModel: CountryListViewModel = hiltViewModel()
) {
    Dialog(
        onDismissRequest = {
            onEvent.invoke(CountrySelectionEvent.Dismiss)
        }
    ) {
        CountryCurrencyListAndSearchView(
            modifier = modifier,
            viewModel = countryListViewModel,
            onEvent = onEvent
        )
    }
}