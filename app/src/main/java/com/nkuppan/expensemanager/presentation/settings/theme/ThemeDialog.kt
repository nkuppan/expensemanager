package com.nkuppan.expensemanager.presentation.settings.theme

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.Theme
import com.nkuppan.expensemanager.ui.theme.ExpenseManagerTheme


@Composable
fun ThemeDialogView(
    complete: () -> Unit
) {

    val viewModel: ThemeViewModel = hiltViewModel()

    val selectedTheme by viewModel.currentTheme.collectAsState()
    val themes by viewModel.themes.collectAsState()

    ThemeDialogViewContent(
        selectedTheme = selectedTheme,
        themes = themes,
        onConfirm = {
            viewModel.setTheme(it)
            complete.invoke()
        }
    )
}

@Composable
fun ThemeDialogViewContent(
    onConfirm: (Theme?) -> Unit,
    selectedTheme: Theme,
    themes: List<Theme> = emptyList()
) {
    Dialog(
        onDismissRequest = {
            onConfirm.invoke(null)
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
    ) {
        Surface(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.wrapContentSize()
            ) {
                item {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        text = stringResource(id = R.string.choose_theme),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(themes) { theme ->
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
                                            color = colorResource(id = R.color.green_500).copy(alpha = .1f),
                                            shape = RoundedCornerShape(size = 12.dp)
                                        )
                                } else {
                                    Modifier
                                        .padding(4.dp)
                                }
                            )
                            .padding(12.dp),
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = stringResource(id = theme.titleResId)
                        )
                        if (isThemeSelected) {
                            Icon(
                                modifier = Modifier.align(Alignment.CenterVertically),
                                painter = painterResource(id = R.drawable.ic_done),
                                contentDescription = null
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
    ExpenseManagerTheme {
        ThemeDialogViewContent(
            onConfirm = {},
            selectedTheme = Theme(AppCompatDelegate.MODE_NIGHT_NO, R.string.light),
            themes = listOf(
                Theme(AppCompatDelegate.MODE_NIGHT_NO, R.string.light),
                Theme(AppCompatDelegate.MODE_NIGHT_YES, R.string.dark),
                Theme(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, R.string.set_by_battery_saver),
                Theme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, R.string.system_default)
            )
        )
    }
}