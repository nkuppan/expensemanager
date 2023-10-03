package com.nkuppan.expensemanager.presentation.settings

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.extensions.launchReviewWorkflow
import com.nkuppan.expensemanager.core.ui.extensions.openEmailToOption
import com.nkuppan.expensemanager.core.ui.extensions.openWebPage
import com.nkuppan.expensemanager.core.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.core.ui.theme.widget.TopNavigationBar
import com.nkuppan.expensemanager.domain.model.Currency
import com.nkuppan.expensemanager.domain.model.Theme
import com.nkuppan.expensemanager.presentation.settings.currency.CurrencyDialogView
import com.nkuppan.expensemanager.presentation.settings.theme.ThemeDialogView
import com.nkuppan.expensemanager.presentation.settings.time.TimePickerView


@Composable
fun SettingsScreen(
    navController: NavController
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val currency by viewModel.currency.collectAsState()
    val theme by viewModel.theme.collectAsState()
    SettingsScreenScaffoldView(navController, currency, theme)
}

@Composable
private fun SettingsScreenScaffoldView(
    navController: NavController,
    currency: Currency? = null,
    theme: Theme? = null,
) {
    val context = LocalContext.current

    var showThemeSelection by remember { mutableStateOf(false) }
    if (showThemeSelection) {
        ThemeDialogView {
            showThemeSelection = false
        }
    }

    var showCurrencySelection by remember { mutableStateOf(false) }
    if (showCurrencySelection) {
        CurrencyDialogView {
            showCurrencySelection = false
        }
    }

    var showTimePickerSelection by remember { mutableStateOf(false) }
    if (showTimePickerSelection) {
        TimePickerView {
            showTimePickerSelection = false
        }
    }

    Scaffold(
        topBar = {
            TopNavigationBar(
                navController = navController,
                title = stringResource(R.string.settings)
            )
        }
    ) { innerPadding ->
        SettingsScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            currency = currency,
            theme = theme,
        ) {
            when (it) {
                SettingOption.THEME -> {
                    showThemeSelection = true
                }

                SettingOption.CURRENCY -> {
                    showCurrencySelection = true
                }

                SettingOption.NOTIFICATION -> {
                    showTimePickerSelection = true
                }

                SettingOption.INFO -> {
                    openWebPage(context, "http://naveenapps.com/")
                }

                SettingOption.RATE_US -> {
                    launchReviewWorkflow(context)
                }

                SettingOption.GITHUB -> {
                    openWebPage(context, "https://www.github.com/nkuppan")
                }

                SettingOption.TWITTER -> {
                    openWebPage(context, "https://www.twitter.com/naveenkumarn27")
                }

                SettingOption.INSTAGRAM -> {
                    openWebPage(context, "https://www.instagram.com/naveenkumar_kup")
                }

                SettingOption.MAIL -> {
                    openEmailToOption(context, "naveenkumarn2@gmail.com")
                }
            }
        }
    }
}

@Composable
private fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    currency: Currency? = null,
    theme: Theme? = null,
    settingOptionSelected: ((SettingOption) -> Unit)? = null
) {
    Column(modifier = modifier) {
        SettingsItem(
            modifier = Modifier
                .clickable {
                    settingOptionSelected?.invoke(SettingOption.THEME)
                }
                .padding(top = 8.dp, bottom = 8.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.theme),
            description = if (theme != null)
                (stringResource(id = theme.titleResId))
            else
                stringResource(id = R.string.system_default),
            icon = R.drawable.ic_palette
        )
        SettingsItem(
            modifier = Modifier
                .clickable {
                    settingOptionSelected?.invoke(SettingOption.CURRENCY)
                }
                .padding(top = 8.dp, bottom = 8.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.currency),
            description = stringResource(id = R.string.select_currency),
            icon = currency?.icon ?: R.drawable.currency_dollar
        )
        SettingsItem(
            modifier = Modifier
                .clickable {
                    settingOptionSelected?.invoke(SettingOption.NOTIFICATION)
                }
                .padding(top = 8.dp, bottom = 8.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.notification),
            description = stringResource(id = R.string.selected_daily_reminder_time),
            icon = R.drawable.ic_edit_notifications
        )
        SettingsItem(
            modifier = Modifier
                .clickable {
                    settingOptionSelected?.invoke(SettingOption.INFO)
                }
                .padding(top = 8.dp, bottom = 8.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.info),
            description = stringResource(id = R.string.about_the_app_information),
            icon = R.drawable.ic_info
        )
        SettingsItem(
            modifier = Modifier
                .clickable {
                    settingOptionSelected?.invoke(SettingOption.RATE_US)
                }
                .padding(top = 8.dp, bottom = 8.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.rate_us),
            description = stringResource(id = R.string.rate_us_message),
            icon = R.drawable.ic_rate
        )

        DeveloperInfoView(settingOptionSelected)
    }
}

@Composable
private fun DeveloperInfoView(
    settingOptionSelected: ((SettingOption) -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterHorizontally),
            text = stringResource(id = R.string.developed_by)
        )
        Row(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp)
        ) {
            IconButton(onClick = {
                settingOptionSelected?.invoke(SettingOption.GITHUB)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_github),
                    contentDescription = ""
                )
            }
            IconButton(onClick = {
                settingOptionSelected?.invoke(SettingOption.TWITTER)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_twitter),
                    contentDescription = ""
                )
            }
            IconButton(onClick = {
                settingOptionSelected?.invoke(SettingOption.INSTAGRAM)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_instagram),
                    contentDescription = ""
                )
            }
            IconButton(onClick = {
                settingOptionSelected?.invoke(SettingOption.MAIL)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_mail),
                    contentDescription = ""
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    description: String,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Icon(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp),
            painter = painterResource(id = icon),
            contentDescription = null
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
        ) {
            Text(text = title)
            Text(text = description, fontSize = 12.sp)
        }
    }
}

private enum class SettingOption {
    THEME,
    CURRENCY,
    NOTIFICATION,
    INFO,
    RATE_US,
    GITHUB,
    TWITTER,
    INSTAGRAM,
    MAIL
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenItemPreview() {
    ExpenseManagerTheme {
        SettingsItem(
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.theme),
            description = stringResource(id = R.string.system_default),
            icon = R.drawable.ic_palette
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenPreview() {
    ExpenseManagerTheme {
        SettingsScreenScaffoldView(rememberNavController())
    }
}