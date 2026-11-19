package com.naveenapps.expensemanager.feature.theme

import androidx.appcompat.app.AppCompatDelegate
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
import com.naveenapps.expensemanager.core.model.Theme

@Composable
fun ThemeDialogView(
    complete: () -> Unit,
) {
    val viewModel: ThemeViewModel = org.koin.androidx.compose.koinViewModel()

    val selectedTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val themes by viewModel.themes.collectAsStateWithLifecycle()

    ThemeDialogViewContent(
        selectedTheme = selectedTheme,
        themes = themes,
        onConfirm = {
            viewModel.setTheme(it)
            complete.invoke()
        },
    )
}

@Composable
fun ThemeDialogViewContent(
    onConfirm: (Theme?) -> Unit,
    selectedTheme: Theme,
    themes: List<Theme> = emptyList(),
) {
    // Choosing closes the sheet; closing it without choosing reports null.
    var chosen by remember { mutableStateOf(false) }
    EmSelectionSheet(
        title = stringResource(id = R.string.choose_theme),
        options = themes.map { stringResource(id = it.titleResId) },
        selectedIndex = themes.indexOfFirst { it.mode == selectedTheme.mode },
        onSelect = {
            chosen = true
            onConfirm(themes[it])
        },
        onDismissRequest = { if (!chosen) onConfirm(null) },
    )
}

@Preview
@Composable
fun ThemeDialogViewPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        ThemeDialogViewContent(
            onConfirm = {},
            selectedTheme = Theme(
                AppCompatDelegate.MODE_NIGHT_NO,
                R.string.choose_theme,
            ),
            themes = listOf(
                Theme(
                    AppCompatDelegate.MODE_NIGHT_NO,
                    R.string.choose_theme,
                ),
                Theme(
                    AppCompatDelegate.MODE_NIGHT_YES,
                    R.string.choose_theme,
                ),
                Theme(
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY,
                    R.string.choose_theme,
                ),
                Theme(
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                    R.string.choose_theme,
                ),
            ),
        )
    }
}
