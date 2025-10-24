package com.naveenapps.expensemanager.feature.country

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CountryCurrencySelectionDialog(
    modifier: Modifier = Modifier,
    onEvent: (CountrySelectionEvent) -> Unit,
    countryListViewModel: CountryListViewModel = koinViewModel()
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