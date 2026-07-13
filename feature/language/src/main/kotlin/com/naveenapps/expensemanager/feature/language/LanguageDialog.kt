package com.naveenapps.expensemanager.feature.language

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.model.AppLocale

@Composable
fun LanguageDialogView(
    complete: () -> Unit,
) {
    val viewModel: LanguageViewModel = org.koin.androidx.compose.koinViewModel()

    val selectedLocale by viewModel.currentLocale.collectAsState()
    val locales by viewModel.locales.collectAsState()

    LanguageDialogViewContent(
        selectedLocale = selectedLocale,
        locales = locales,
        onConfirm = {
            viewModel.setLocale(it)
            complete.invoke()
        },
    )
}

@Composable
fun LanguageDialogViewContent(
    onConfirm: (AppLocale?) -> Unit,
    selectedLocale: AppLocale,
    locales: List<AppLocale> = emptyList(),
) {
    Dialog(
        onDismissRequest = {
            onConfirm.invoke(null)
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        AppCardView(modifier = Modifier.padding(16.dp)) {
            LazyColumn(
                modifier = Modifier.wrapContentSize(),
            ) {
                item {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        text = stringResource(id = R.string.choose_language),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                items(locales, key = { it.tag }) { locale ->
                    val isLocaleSelected = selectedLocale.tag == locale.tag
                    Row(
                        modifier = Modifier
                            .clickable {
                                onConfirm.invoke(locale)
                            }
                            .fillMaxWidth()
                            .then(
                                if (isLocaleSelected) {
                                    Modifier
                                        .padding(4.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.secondary,
                                            shape = RoundedCornerShape(size = 12.dp),
                                        )
                                } else {
                                    Modifier
                                        .padding(4.dp)
                                },
                            )
                            .padding(12.dp),
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = stringResource(id = locale.titleResId),
                            color = if (isLocaleSelected) {
                                MaterialTheme.colorScheme.onSecondary
                            } else {
                                Color.Unspecified
                            }
                        )
                        if (isLocaleSelected) {
                            Icon(
                                modifier = Modifier.align(Alignment.CenterVertically),
                                imageVector = Icons.Default.Done,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun LanguageDialogViewPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        LanguageDialogViewContent(
            onConfirm = {},
            selectedLocale = AppLocale("", R.string.choose_language),
            locales = listOf(
                AppLocale("", R.string.choose_language),
                AppLocale("en", R.string.choose_language),
                AppLocale("es", R.string.choose_language),
            ),
        )
    }
}
