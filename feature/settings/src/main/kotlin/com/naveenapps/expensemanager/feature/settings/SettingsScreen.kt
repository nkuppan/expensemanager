package com.naveenapps.expensemanager.feature.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNotifications
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.SettingsApplications
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBar
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Theme
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

            SettingOption.CURRENCY -> {
                viewModel.openCurrencyCustomiseScreen()
            }

            SettingOption.EXPORT -> {
                viewModel.openExportScreen()
            }

            SettingOption.REMINDER -> {
                viewModel.openReminderScreen()
            }

            SettingOption.ABOUT_US -> {
                viewModel.openAboutUs()
            }

            SettingOption.ADVANCED -> {
                viewModel.openAdvancedSettings()
            }

            else -> Unit
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreenScaffoldView(
    currency: Currency,
    theme: Theme? = null,
    settingOptionSelected: ((SettingOption) -> Unit)? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showThemeSelection by remember { mutableStateOf(false) }
    if (showThemeSelection) {
        ThemeDialogView {
            showThemeSelection = false
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
            windowInsets = WindowInsets(0.dp),
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
                title = stringResource(R.string.settings),
            )
        },
    ) { innerPadding ->
        SettingsScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding),
            selectedCurrency = currency,
            theme = theme,
        ) {
            when (it) {
                SettingOption.THEME -> {
                    showThemeSelection = true
                }

                SettingOption.FILTER -> {
                    scope.launch {
                        bottomSheetState.show()
                        showDateFilter = true
                    }
                }

                SettingOption.RATE_US -> {
                    launchReviewWorkflow(context)
                }

                else -> {
                    settingOptionSelected?.invoke(it)
                }
            }
        }
    }
}

@Composable
private fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    selectedCurrency: Currency,
    theme: Theme? = null,
    settingOptionSelected: ((SettingOption) -> Unit)? = null,
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
            description = if (theme != null) {
                (stringResource(id = theme.titleResId))
            } else {
                stringResource(id = R.string.system_default)
            },
            imageVector = Icons.Outlined.Palette,
        )
        SettingsItem(
            modifier = Modifier
                .clickable {
                    settingOptionSelected?.invoke(SettingOption.CURRENCY)
                }
                .padding(top = 8.dp, bottom = 8.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.currency),
            description = "${selectedCurrency.name}(${selectedCurrency.symbol})",
            imageVector = Icons.Outlined.Payments,
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
            imageVector = Icons.Outlined.EditNotifications,
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
            imageVector = Icons.Outlined.FilterAlt,
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
            imageVector = Icons.Outlined.Upload,
        )
        SettingsItem(
            modifier = Modifier
                .clickable {
                    settingOptionSelected?.invoke(SettingOption.ABOUT_US)
                }
                .padding(top = 8.dp, bottom = 8.dp)
                .fillMaxWidth(),
            title = stringResource(id = com.naveenapps.expensemanager.feature.about.R.string.about_us),
            description = stringResource(id = R.string.about_the_app_information),
            imageVector = Icons.Outlined.Info,
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
            imageVector = Icons.Outlined.RateReview,
        )
        SettingsItem(
            modifier = Modifier
                .clickable {
                    settingOptionSelected?.invoke(SettingOption.ADVANCED)
                }
                .padding(top = 8.dp, bottom = 8.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.advanced),
            description = stringResource(id = R.string.advanced_config_message),
            imageVector = Icons.Outlined.SettingsApplications,
        )
    }
}

@Composable
private fun SettingsItem(
    title: String,
    description: String,
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        Icon(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp),
            imageVector = imageVector,
            contentDescription = null,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically),
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
    ABOUT_US,
    RATE_US,
    ADVANCED,
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
            imageVector = Icons.Outlined.Upload,
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenPreview() {
    ExpenseManagerTheme {
        SettingsScreenScaffoldView(
            currency = Currency("$", "US Dollar"),
        )
    }
}

fun launchReviewWorkflow(context: Context) {
    val packageName = context.packageName
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
    } catch (e: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
    }
}
