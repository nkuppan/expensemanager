package com.naveenapps.expensemanager.feature.country

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.ui.components.SafeModalBottomSheet
import org.koin.androidx.compose.koinViewModel

/**
 * @param message optional explanation shown above the list, for pickers whose purpose isn't
 * obvious from context (e.g. "which currency is this backup in?").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryCurrencySelectionBottomSheet(
    modifier: Modifier = Modifier,
    message: String? = null,
    onEvent: (CountrySelectionEvent) -> Unit,
    countryListViewModel: CountryListViewModel = koinViewModel(),
) {
    SafeModalBottomSheet(
        onDismissRequest = {
            onEvent.invoke(CountrySelectionEvent.Dismiss)
        },
    ) {
        Column {
            if (message != null) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            CountryCurrencyListAndSearchView(
                modifier = modifier,
                viewModel = countryListViewModel,
                onEvent = onEvent,
            )
        }
    }
}
