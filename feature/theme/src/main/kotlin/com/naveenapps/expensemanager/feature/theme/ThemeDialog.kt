package com.naveenapps.expensemanager.feature.theme

import androidx.appcompat.app.AppCompatDelegate
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
import com.naveenapps.expensemanager.core.model.Theme

@Composable
fun ThemeDialogView(
    complete: () -> Unit,
) {
    val viewModel: ThemeViewModel = org.koin.androidx.compose.koinViewModel()

    val selectedTheme by viewModel.currentTheme.collectAsState()
    val themes by viewModel.themes.collectAsState()

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
                        text = stringResource(id = R.string.choose_theme),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                items(themes, key = { it.mode }) { theme ->
                    val isThemeSelected = selectedTheme.mode == theme.mode
                    Row(
                        modifier = Modifier
                            .clickable {
                                onConfirm.invoke(theme)
                            }
                            .fillMaxWidth()
                            .then(
                                if (isThemeSelected) {
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
                            text = stringResource(id = theme.titleResId),
                            color = if (isThemeSelected) {
                                MaterialTheme.colorScheme.onSecondary
                            } else {
                                Color.Unspecified
                            }
                        )
                        if (isThemeSelected) {
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
fun ThemeDialogViewPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
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
