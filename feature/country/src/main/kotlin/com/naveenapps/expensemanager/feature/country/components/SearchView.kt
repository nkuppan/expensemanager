package com.naveenapps.expensemanager.feature.country.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.feature.country.CountrySelectionAction
import com.naveenapps.expensemanager.feature.country.CountryState
import com.naveenapps.expensemanager.feature.country.R


@Composable
internal fun CountrySearchView(
    state: CountryState,
    onAction: (CountrySelectionAction) -> Unit
) {
    Surface(shadowElevation = 8.dp) {
        Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 4.dp)) {
            OutlinedTextField(
                modifier = Modifier
                    .background(color = Color.Transparent)
                    .weight(1f),
                value = state.searchText.value,
                onValueChange = {
                    state.searchText.onValueChange?.invoke(it)
                },
                label = {
                    Text(
                        modifier = Modifier.align(
                            Alignment.CenterVertically
                        ),
                        text = stringResource(
                            id = R.string.search_country
                        ),
                        textAlign = TextAlign.Center
                    )
                },
                leadingIcon = {
                    Icon(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp)
                            .align(Alignment.CenterVertically)
                            .clickable {
                                onAction.invoke(CountrySelectionAction.ClosePage)
                            },
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = ""
                    )
                },
                trailingIcon = {
                    if (state.showClearButton) {
                        Icon(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp)
                                .align(Alignment.CenterVertically)
                                .clickable {
                                    onAction.invoke(CountrySelectionAction.ClearText)
                                },
                            imageVector = Icons.Default.Close,
                            contentDescription = ""
                        )
                    }
                }
            )
        }
    }
}