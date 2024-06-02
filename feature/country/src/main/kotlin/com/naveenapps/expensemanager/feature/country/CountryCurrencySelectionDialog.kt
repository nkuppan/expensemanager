package com.naveenapps.expensemanager.feature.country

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.model.Country

@Composable
fun CountryCurrencySelectionDialog(
    modifier: Modifier = Modifier,
    countryListViewModel: CountryListViewModel = hiltViewModel(),
    selection: ((Country?) -> Unit)? = null
) {
    Dialog(
        onDismissRequest = {
            selection?.invoke(null)
        }
    ) {
        CountryCurrencyListAndSearchView(
            modifier = modifier,
            countryListViewModel = countryListViewModel,
            selection = selection
        )
    }
}