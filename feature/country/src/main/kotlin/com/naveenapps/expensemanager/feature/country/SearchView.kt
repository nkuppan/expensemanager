package com.naveenapps.expensemanager.feature.country

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.model.TextFieldValue


@Composable
internal fun CountrySearchView(
    searchText: TextFieldValue<String>,
    dismiss: (() -> Unit)? = null
) {

    var showClear by remember { mutableStateOf(false) }

    Surface(shadowElevation = 8.dp) {
        Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 4.dp)) {
            OutlinedTextField(
                modifier = Modifier
                    .background(color = Color.Transparent)
                    .weight(1f),
                value = searchText.value,
                onValueChange = {
                    searchText.onValueChange?.invoke(it)
                    showClear = it.isNotBlank()
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
                                dismiss?.invoke()
                            },
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = ""
                    )
                },
                trailingIcon = {
                    if (showClear) {
                        Icon(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp)
                                .align(Alignment.CenterVertically)
                                .clickable {
                                    searchText.onValueChange?.invoke("")
                                    showClear = false
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