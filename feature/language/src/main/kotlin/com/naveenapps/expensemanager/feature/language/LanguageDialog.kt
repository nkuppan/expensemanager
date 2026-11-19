package com.naveenapps.expensemanager.feature.language

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.em.EmSelectionSheet
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.model.AppLocale

@Composable
fun LanguageDialogView(
    complete: () -> Unit,
) {
    val viewModel: LanguageViewModel = org.koin.androidx.compose.koinViewModel()

    val selectedLocale by viewModel.currentLocale.collectAsStateWithLifecycle()
    val locales by viewModel.locales.collectAsStateWithLifecycle()

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
    // Choosing closes the sheet; closing it without choosing reports null.
    var chosen by remember { mutableStateOf(false) }
    EmSelectionSheet(
        title = stringResource(id = R.string.choose_language),
        options = locales.map { stringResource(id = it.titleResId) },
        selectedIndex = locales.indexOfFirst { it.tag == selectedLocale.tag },
        onSelect = {
            chosen = true
            onConfirm(locales[it])
        },
        onDismissRequest = { if (!chosen) onConfirm(null) },
    )
}

@Preview
@Composable
fun LanguageDialogViewPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
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
