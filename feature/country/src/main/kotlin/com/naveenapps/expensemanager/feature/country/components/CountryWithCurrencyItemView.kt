package com.naveenapps.expensemanager.feature.country.components

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme


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


@Preview(uiMode = UI_MODE_NIGHT_YES)
@Preview(uiMode = UI_MODE_NIGHT_NO)
@Composable
fun CountryWithCurrencyItemViewPreview() {
    val context = LocalContext.current
    NaveenAppsPreviewTheme(padding = 0.dp) {
        CountryWithCurrencyItemView(
            modifier = Modifier.padding(16.dp),
            name = "India",
            description = "Currency: Rupee (₹)"
        )
    }
}