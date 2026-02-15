package com.naveenapps.expensemanager.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.EditNotifications
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.SettingsApplications
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingRow
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Theme
import com.naveenapps.expensemanager.core.repository.ShareRepository
import com.naveenapps.expensemanager.feature.theme.ThemeDialogView
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    shareRepository: ShareRepository,
    viewModel: SettingsViewModel = koinViewModel()
) {

    val state by viewModel.state.collectAsState()

    ObserveAsEvents(viewModel.event) {
        when (it) {
            SettingEvent.RateUs -> {
                shareRepository.openRateUs()
            }
        }
    }

    SettingsScreenScaffoldView(
        state = state,
        onAction = viewModel::processAction
    )
}

@Composable
private fun SettingsScreenScaffoldView(
    state: SettingState,
    onAction: (SettingAction) -> Unit,
) {
    if (state.showThemeSelection) {
        ThemeDialogView {
            onAction.invoke(SettingAction.DismissThemeSelection)
        }
    }

    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = {
                    onAction.invoke(SettingAction.ClosePage)
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
            selectedCurrency = state.currency,
            theme = state.theme,
            onAction = onAction
        )
    }
}

@Composable
private fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    selectedCurrency: Currency,
    theme: Theme? = null,
    onAction: (SettingAction) -> Unit,
) {
    Box(modifier) {
        AppCardView(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 16.dp
            )
        ) {
            Column {
                SettingRow(
                    onClick = {
                        onAction.invoke(SettingAction.ShowThemeSelection)
                    },
                    title = stringResource(id = R.string.theme),
                    subtitle = if (theme != null) {
                        (stringResource(id = theme.titleResId))
                    } else {
                        stringResource(id = R.string.system_default)
                    },
                    icon = Icons.Outlined.Palette,
                    showDivider = true,
                )
                SettingRow(
                    onClick = {
                        onAction.invoke(SettingAction.OpenCurrencyEdit)
                    },
                    title = stringResource(id = R.string.currency),
                    subtitle = "${selectedCurrency.name}(${selectedCurrency.symbol})",
                    icon = Icons.Outlined.Payments,
                    showDivider = true,
                )
                SettingRow(
                    onClick = {
                        onAction.invoke(SettingAction.OpenNotification)
                    },
                    title = stringResource(id = R.string.reminder_notification),
                    subtitle = stringResource(id = R.string.selected_daily_reminder_time),
                    icon = Icons.Outlined.EditNotifications,
                    showDivider = true,
                )
                SettingRow(
                    onClick = {
                        onAction.invoke(SettingAction.OpenExport)
                    },
                    title = stringResource(id = R.string.export),
                    subtitle = stringResource(id = R.string.export_message),
                    icon = Icons.Outlined.Upload,
                    showDivider = true,
                )
                SettingRow(
                    onClick = {
                        onAction.invoke(SettingAction.OpenRateUs)
                    },
                    title = stringResource(id = R.string.rate_us),
                    subtitle = stringResource(id = R.string.rate_us_message),
                    icon = Icons.Outlined.RateReview,
                    showDivider = true,
                )
                SettingRow(
                    onClick = {
                        onAction.invoke(SettingAction.OpenAdvancedSettings)
                    },
                    title = stringResource(id = R.string.advanced),
                    subtitle = stringResource(id = R.string.advanced_config_message),
                    icon = Icons.Outlined.SettingsApplications,
                    showDivider = true,
                )
                SettingRow(
                    onClick = {
                        onAction.invoke(SettingAction.OpenAboutUs)
                    },
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                    title = stringResource(id = com.naveenapps.expensemanager.feature.about.R.string.about_us),
                    subtitle = stringResource(id = R.string.about_the_app_information),
                    icon = Icons.Outlined.Info,
                )
            }
        }
    }
}


@AppPreviewsLightAndDarkMode
@Composable
fun SettingsScreenPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        SettingsScreenScaffoldView(
            state = SettingState(
                currency = Currency("$", "US Dollar"),
                theme = null,
                showThemeSelection = false
            ),
            onAction = {}
        )
    }
}
