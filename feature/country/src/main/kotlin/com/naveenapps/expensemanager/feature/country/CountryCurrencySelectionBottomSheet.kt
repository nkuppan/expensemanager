package com.naveenapps.invoicebuilder.feature.country

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.naveenapps.expensemanager.feature.country.CountryCurrencyListAndSearchView
import com.naveenapps.expensemanager.feature.country.CountryListViewModel
import com.naveenapps.expensemanager.feature.country.CountrySelectionEvent
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryCurrencySelectionBottomSheet(
    modifier: Modifier = Modifier,
    onEvent: (CountrySelectionEvent) -> Unit,
    countryListViewModel: CountryListViewModel = koinViewModel()
) {
    ModalBottomSheet(
        onDismissRequest = {
            onEvent.invoke(CountrySelectionEvent.Dismiss)
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        CountryCurrencyListAndSearchView(
            modifier = modifier,
            viewModel = countryListViewModel,
            onEvent = onEvent
        )
    }
}