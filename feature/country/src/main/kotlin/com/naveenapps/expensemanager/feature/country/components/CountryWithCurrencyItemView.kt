package com.naveenapps.expensemanager.feature.country.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme


@Composable
internal fun CountryWithCurrencyItemView(
    name: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(text = name)
        Text(
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            text = description,
        )
    }
}


@AppPreviewsLightAndDarkMode
@Composable
fun CountryWithCurrencyItemViewPreview() {
    ExpenseManagerTheme {
        CountryWithCurrencyItemView(
            modifier = Modifier.padding(16.dp),
            name = "India",
            description = "Currency: Rupee (₹)"
        )
    }
}