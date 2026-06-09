package com.naveenapps.expensemanager.feature.country

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.naveenapps.expensemanager.core.designsystem.ui.components.SafeModalBottomSheet
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryCurrencySelectionBottomSheet(
    modifier: Modifier = Modifier,
    onEvent: (CountrySelectionEvent) -> Unit,
    countryListViewModel: CountryListViewModel = koinViewModel()
) {
    SafeModalBottomSheet(
        onDismissRequest = {
            onEvent.invoke(CountrySelectionEvent.Dismiss)
        },
    ) {
        CountryCurrencyListAndSearchView(
            modifier = modifier,
            viewModel = countryListViewModel,
            onEvent = onEvent
        )
    }
}