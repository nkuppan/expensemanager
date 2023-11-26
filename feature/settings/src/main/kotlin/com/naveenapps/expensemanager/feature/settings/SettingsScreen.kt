package com.naveenapps.expensemanager.feature.settings

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.play.core.review.ReviewManagerFactory
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBar
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.getAppVersionName
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.openEmailToOption
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.openWebPage
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Theme
import com.naveenapps.expensemanager.feature.currency.CurrencyDialogView
import com.naveenapps.expensemanager.feature.datefilter.DateFilterSelectionView
import com.naveenapps.expensemanager.feature.theme.ThemeDialogView
import kotlinx.coroutines.launch


@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = hiltViewModel()
    val currency by viewModel.currency.collectAsState()
    val theme by viewModel.theme.collectAsState()
    SettingsScreenScaffoldView(currency, theme) {
        when (it) {
            SettingOption.BACK -> {
                viewModel.closePage()
            }

            SettingOption.EXPORT -> {
                viewModel.openExportScreen()
            }

            SettingOption.REMINDER -> {
                viewModel.openReminderScreen()
            }

            else -> Unit
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreenScaffoldView(
    currency: Currency? = null,
    theme: Theme? = null,
    settingOptionSelected: ((SettingOption) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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

    var showDateFilter by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showDateFilter) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    showDateFilter = false
                    bottomSheetState.hide()
                }
            },
            sheetState = bottomSheetState,
            windowInsets = WindowInsets(0.dp)
        ) {
            DateFilterSelectionView {
                scope.launch {
                    showDateFilter = false
                    bottomSheetState.hide()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopNavigationBar(
                onClick = {
                    settingOptionSelected?.invoke(SettingOption.BACK)
                },
                title = stringResource(R.string.settings)
            )
        }
    ) { innerPadding ->
        SettingsScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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

                SettingOption.REMINDER -> {
                    settingOptionSelected?.invoke(it)
                }

                SettingOption.FILTER -> {
                    scope.launch {
                        bottomSheetState.show()
                        showDateFilter = true
                    }
                }

                SettingOption.EXPORT -> {
                    settingOptionSelected?.invoke(it)
                }
                //TODO Want to replace them with remote config
                SettingOption.INFO -> {
                    openWebPage(context, "https://expensemanager.naveenapps.com/")
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

                else -> Unit
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
            icon = currency?.symbolIcon
                ?: com.naveenapps.expensemanager.core.common.R.drawable.currency_dollar
        )
        SettingsItem(
            modifier = Modifier
                .clickable {
                    settingOptionSelected?.invoke(SettingOption.REMINDER)
                }
                .padding(top = 8.dp, bottom = 8.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.reminder_notification),
            description = stringResource(id = R.string.selected_daily_reminder_time),
            icon = R.drawable.ic_edit_notifications
        )
        SettingsItem(
            modifier = Modifier
                .clickable {
                    settingOptionSelected?.invoke(SettingOption.FILTER)
                }
                .padding(top = 8.dp, bottom = 8.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.filter),
            description = stringResource(id = R.string.filter_message),
            icon = R.drawable.ic_filter
        )
        SettingsItem(
            modifier = Modifier
                .clickable {
                    settingOptionSelected?.invoke(SettingOption.EXPORT)
                }
                .padding(top = 8.dp, bottom = 8.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.export),
            description = stringResource(id = R.string.export_message),
            icon = R.drawable.ic_export
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
    val context = LocalContext.current

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
        Text(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
            text = stringResource(
                id = R.string.app_version,
                context.getAppVersionName()
            )
        )
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
            Text(text = description, style = MaterialTheme.typography.labelMedium)
        }
    }
}

private enum class SettingOption {
    BACK,
    THEME,
    CURRENCY,
    REMINDER,
    FILTER,
    EXPORT,
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
        SettingsScreenScaffoldView()
    }
}


fun launchReviewWorkflow(context: Context) {
    val manager = ReviewManagerFactory.create(context)
    val request = manager.requestReviewFlow()
    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            // We got the ReviewInfo object
            val reviewInfo = task.result
            reviewInfo?.let {
                val flow = manager.launchReviewFlow(context as Activity, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                }
            }
        }
    }
}