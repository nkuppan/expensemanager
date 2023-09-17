package com.nkuppan.expensemanager.presentation.settings.theme

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.nkuppan.expensemanager.core.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.domain.model.Theme


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
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.choose_theme),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            repeat(themes.size) {
                val theme = themes[it]
                Row(
                    modifier = Modifier
                        .clickable {
                            onConfirm.invoke(theme)
                        }
                        .fillMaxWidth(),
                ) {
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 16.dp, bottom = 16.dp),
                        text = stringResource(id = theme.titleResId)
                    )
                    if (selectedTheme.mode == theme.mode) {
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