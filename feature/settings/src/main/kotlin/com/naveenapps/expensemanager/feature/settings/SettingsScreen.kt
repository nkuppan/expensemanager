package com.naveenapps.expensemanager.feature.settings

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.EditNotifications
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.ScreenshotMonitor
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.common.utils.AppShareLink
import com.naveenapps.expensemanager.core.common.utils.shareAppMessage
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingRow
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingToggleRow
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingsSection
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.repository.ShareRepository
import com.naveenapps.expensemanager.feature.language.LanguageDialogView
import com.naveenapps.expensemanager.feature.theme.ThemeDialogView
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    shareRepository: ShareRepository,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showMessage(messageResId: Int) {
        scope.launch { snackbarHostState.showSnackbar(context.getString(messageResId)) }
    }

    ObserveAsEvents(viewModel.event) {
        when (it) {
            SettingEvent.RateUs -> {
                shareRepository.openRateUs()
            }

            SettingEvent.ShareApp -> {
                context.shareApp()
            }

            is SettingEvent.ShowMessage -> {
                showMessage(it.messageResId)
            }
        }
    }

    SettingsScreenScaffoldView(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::processAction,
    )
}

@Composable
private fun SettingsScreenScaffoldView(
    state: SettingState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (SettingAction) -> Unit,
) {
    if (state.showDemoDataConfirmation) {
        AlertDialog(
            onDismissRequest = { onAction.invoke(SettingAction.DismissDemoData) },
            title = { Text(text = stringResource(id = R.string.demo_data_confirm_title)) },
            text = { Text(text = stringResource(id = R.string.demo_data_confirm_message)) },
            confirmButton = {
                TextButton(onClick = { onAction.invoke(SettingAction.ConfirmDemoData) }) {
                    Text(text = stringResource(id = R.string.demo_data_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction.invoke(SettingAction.DismissDemoData) }) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            },
        )
    }

    if (state.showThemeSelection) {
        ThemeDialogView {
            onAction.invoke(SettingAction.DismissThemeSelection)
        }
    }

    if (state.showLanguageSelection) {
        LanguageDialogView {
            onAction.invoke(SettingAction.DismissLanguageSelection)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            state = state,
            onAction = onAction,
        )
    }
}

@Composable
private fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    state: SettingState,
    onAction: (SettingAction) -> Unit,
) {
    // Four short groups. Values sit at the end of the row and only toggles keep a one-line
    // explanation; everything set once and forgotten lives in Preferences.
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(modifier = Modifier.height(0.dp))

        SettingsSection(title = stringResource(R.string.general)) {
            AppCardView {
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.ShowThemeSelection) },
                    title = stringResource(id = R.string.theme),
                    value = if (state.theme != null) {
                        stringResource(id = state.theme.titleResId)
                    } else {
                        stringResource(id = R.string.system_default)
                    },
                    icon = Icons.Outlined.Palette,
                    showDivider = true,
                )
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.ShowLanguageSelection) },
                    title = stringResource(
                        id = com.naveenapps.expensemanager.feature.language.R.string.language,
                    ),
                    value = if (state.locale != null) {
                        stringResource(id = state.locale.titleResId)
                    } else {
                        stringResource(id = R.string.system_default)
                    },
                    icon = Icons.Outlined.Language,
                    showDivider = true,
                )
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.OpenCurrencyEdit) },
                    title = stringResource(id = R.string.currency),
                    value = state.currency.symbol,
                    icon = Icons.Outlined.Payments,
                    showDivider = true,
                )
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.OpenNotification) },
                    title = stringResource(id = R.string.reminders),
                    icon = Icons.Outlined.EditNotifications,
                )
            }
        }

        SettingsSection(title = stringResource(R.string.your_data)) {
            AppCardView {
                SettingRow(
                    title = stringResource(id = R.string.backup_and_restore),
                    icon = Icons.Outlined.Backup,
                    onClick = { onAction.invoke(SettingAction.OpenBackup) },
                    showDivider = true,
                )
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.OpenExport) },
                    title = stringResource(id = R.string.export),
                    icon = Icons.Outlined.Upload,
                    showDivider = true,
                )
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.OpenRecurringTransactions) },
                    title = stringResource(id = R.string.recurring_transactions_setting),
                    icon = Icons.Outlined.EventRepeat,
                )
            }
        }

        SettingsSection(title = stringResource(R.string.security)) {
            AppCardView {
                SettingToggleRow(
                    title = stringResource(id = R.string.app_lock),
                    subtitle = stringResource(id = R.string.app_lock_message),
                    icon = Icons.Outlined.Lock,
                    checked = state.isAppLockEnabled,
                    onCheckedChange = { onAction.invoke(SettingAction.ToggleAppLock) },
                    showDivider = true,
                )
                SettingToggleRow(
                    title = stringResource(id = R.string.block_screenshots),
                    subtitle = stringResource(id = R.string.block_screenshots_message),
                    icon = Icons.Outlined.ScreenshotMonitor,
                    checked = state.isScreenshotBlocked,
                    onCheckedChange = { onAction.invoke(SettingAction.ToggleScreenshotBlock) },
                )
            }
        }

        SettingsSection(title = stringResource(R.string.more)) {
            AppCardView {
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.OpenAdvancedSettings) },
                    title = stringResource(id = R.string.preferences),
                    subtitle = stringResource(id = R.string.preferences_message),
                    icon = Icons.Outlined.Tune,
                    showDivider = true,
                )
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.OpenRateUs) },
                    title = stringResource(id = R.string.rate_us),
                    icon = Icons.Outlined.RateReview,
                    showDivider = true,
                )
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.ShareApp) },
                    title = stringResource(id = R.string.share_app),
                    icon = Icons.Outlined.Share,
                    showDivider = true,
                )
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.OpenAboutUs) },
                    title = stringResource(id = com.naveenapps.expensemanager.feature.about.R.string.about_us),
                    icon = Icons.Outlined.Info,
                )
            }
        }

        // Developer — debug builds only (no DemoDataRepository is bound in release).
        if (state.isDemoDataAvailable) {
            SettingsSection(title = stringResource(R.string.developer)) {
                AppCardView {
                    SettingRow(
                        onClick = { onAction.invoke(SettingAction.LoadDemoData) },
                        title = stringResource(id = R.string.demo_data),
                        subtitle = stringResource(
                            id = if (state.isDemoDataLoading) R.string.demo_data_loading else R.string.demo_data_message,
                        ),
                        icon = Icons.Outlined.BugReport,
                        enabled = !state.isDemoDataLoading,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@AppPreviewsLightAndDarkMode
@Composable
fun SettingsScreenPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        SettingsScreenScaffoldView(
            state = SettingState(
                currency = Currency("$", "US Dollar"),
                theme = null,
                showThemeSelection = false,
            ),
            onAction = {},
        )
    }
}

/** Word-of-mouth: share a short pitch plus the store link (tagged as a Settings share). */
private fun Context.shareApp() {
    shareAppMessage(
        message = getString(R.string.share_app_text, AppShareLink.playStore(AppShareLink.MEDIUM_SETTINGS)),
        chooserTitle = getString(R.string.share_app),
    )
}
