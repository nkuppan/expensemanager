package com.naveenapps.expensemanager.feature.country

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.naveenapps.expensemanager.core.designsystem.em.EmModalSheet
import org.koin.androidx.compose.koinViewModel

@Composable
fun CountryCurrencySelectionBottomSheet(
    modifier: Modifier = Modifier,
    onEvent: (CountrySelectionEvent) -> Unit,
    countryListViewModel: CountryListViewModel = koinViewModel(),
) {
    EmModalSheet(
        onDismissRequest = {
            onEvent.invoke(CountrySelectionEvent.Dismiss)
        },
    ) {
        CountryCurrencyListAndSearchView(
            modifier = modifier,
            viewModel = countryListViewModel,
            onEvent = onEvent,
        )
    }
}
