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
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.EditNotifications
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.ScreenshotMonitor
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.common.utils.AppShareLink
import com.naveenapps.expensemanager.core.common.utils.shareAppMessage
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmConfirm
import com.naveenapps.expensemanager.core.designsystem.em.EmDivider
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSection
import com.naveenapps.expensemanager.core.designsystem.em.EmSettingItem
import com.naveenapps.expensemanager.core.designsystem.em.EmSettingTrailing
import com.naveenapps.expensemanager.core.designsystem.em.EmSnackbarHostState
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
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
    val snackbarHostState = remember { EmSnackbarHostState() }
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
    snackbarHostState: EmSnackbarHostState = remember { EmSnackbarHostState() },
    onAction: (SettingAction) -> Unit,
) {
    if (state.showDemoDataConfirmation) {
        EmConfirm(
            title = stringResource(id = R.string.demo_data_confirm_title),
            message = stringResource(id = R.string.demo_data_confirm_message),
            confirmText = stringResource(id = R.string.demo_data_confirm_action),
            onConfirm = { onAction.invoke(SettingAction.ConfirmDemoData) },
            onDismiss = { onAction.invoke(SettingAction.DismissDemoData) },
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

    EmScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            EmTopBar(
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = {
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
                .padding(horizontal = EmTheme.spacing.lg),
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
        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
    ) {
        Spacer(modifier = Modifier.height(0.dp))

        EmSection(title = stringResource(R.string.general)) {
            EmCard {
                EmSettingItem(
                    icon = Icons.Outlined.Palette,
                    title = stringResource(id = R.string.theme),
                    trailing = EmSettingTrailing.valueOrChevron(
                        if (state.theme != null) {
                            stringResource(id = state.theme.titleResId)
                        } else {
                            stringResource(id = R.string.system_default)
                        },
                    ),
                    onClick = { onAction.invoke(SettingAction.ShowThemeSelection) },
                )
                EmDivider()
                EmSettingItem(
                    icon = Icons.Outlined.Language,
                    title = stringResource(
                        id = com.naveenapps.expensemanager.feature.language.R.string.language,
                    ),
                    trailing = EmSettingTrailing.valueOrChevron(
                        if (state.locale != null) {
                            stringResource(id = state.locale.titleResId)
                        } else {
                            stringResource(id = R.string.system_default)
                        },
                    ),
                    onClick = { onAction.invoke(SettingAction.ShowLanguageSelection) },
                )
                EmDivider()
                EmSettingItem(
                    icon = Icons.Outlined.Payments,
                    title = stringResource(id = R.string.currency),
                    trailing = EmSettingTrailing.valueOrChevron(state.currency.symbol),
                    onClick = { onAction.invoke(SettingAction.OpenCurrencyEdit) },
                )
                EmDivider()
                EmSettingItem(
                    icon = Icons.Outlined.EditNotifications,
                    title = stringResource(id = R.string.reminders),
                    onClick = { onAction.invoke(SettingAction.OpenNotification) },
                )
            }
        }

        EmSection(title = stringResource(R.string.your_data)) {
            EmCard {
                EmSettingItem(
                    icon = Icons.Outlined.Backup,
                    title = stringResource(id = R.string.backup_and_restore),
                    onClick = { onAction.invoke(SettingAction.OpenBackup) },
                )
                EmDivider()
                EmSettingItem(
                    icon = Icons.Outlined.Upload,
                    title = stringResource(id = R.string.export),
                    onClick = { onAction.invoke(SettingAction.OpenExport) },
                )
                EmDivider()
                EmSettingItem(
                    icon = Icons.Outlined.EventRepeat,
                    title = stringResource(id = R.string.recurring_transactions_setting),
                    onClick = { onAction.invoke(SettingAction.OpenRecurringTransactions) },
                )
                EmDivider()
                EmSettingItem(
                    icon = Icons.Outlined.People,
                    title = stringResource(id = R.string.debts_setting),
                    supporting = stringResource(id = R.string.debts_setting_message),
                    onClick = { onAction.invoke(SettingAction.OpenDebts) },
                )
            }
        }

        EmSection(title = stringResource(R.string.security)) {
            EmCard {
                EmSettingItem(
                    icon = Icons.Outlined.Lock,
                    title = stringResource(id = R.string.app_lock),
                    supporting = stringResource(id = R.string.app_lock_message),
                    trailing = EmSettingTrailing.Toggle(state.isAppLockEnabled, { onAction.invoke(SettingAction.ToggleAppLock) }),
                )
                EmDivider()
                EmSettingItem(
                    icon = Icons.Outlined.ScreenshotMonitor,
                    title = stringResource(id = R.string.block_screenshots),
                    supporting = stringResource(id = R.string.block_screenshots_message),
                    trailing = EmSettingTrailing.Toggle(state.isScreenshotBlocked, { onAction.invoke(SettingAction.ToggleScreenshotBlock) }),
                )
            }
        }

        EmSection(title = stringResource(R.string.more)) {
            EmCard {
                EmSettingItem(
                    icon = Icons.Outlined.Tune,
                    title = stringResource(id = R.string.preferences),
                    supporting = stringResource(id = R.string.preferences_message),
                    onClick = { onAction.invoke(SettingAction.OpenAdvancedSettings) },
                )
                EmDivider()
                EmSettingItem(
                    icon = Icons.Outlined.RateReview,
                    title = stringResource(id = R.string.rate_us),
                    onClick = { onAction.invoke(SettingAction.OpenRateUs) },
                )
                EmDivider()
                EmSettingItem(
                    icon = Icons.Outlined.Share,
                    title = stringResource(id = R.string.share_app),
                    onClick = { onAction.invoke(SettingAction.ShareApp) },
                )
                EmDivider()
                EmSettingItem(
                    icon = Icons.Outlined.Info,
                    title = stringResource(id = com.naveenapps.expensemanager.feature.about.R.string.about_us),
                    onClick = { onAction.invoke(SettingAction.OpenAboutUs) },
                )
            }
        }

        // Developer — debug builds only (no DemoDataRepository is bound in release).
        if (state.isDemoDataAvailable) {
            EmSection(title = stringResource(R.string.developer)) {
                EmCard {
                    EmSettingItem(
                        icon = Icons.Outlined.BugReport,
                        title = stringResource(id = R.string.demo_data),
                        supporting = stringResource(
                            id = if (state.isDemoDataLoading) R.string.demo_data_loading else R.string.demo_data_message,
                        ),
                        onClick = { onAction.invoke(SettingAction.LoadDemoData) },
                        enabled = !state.isDemoDataLoading,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(EmTheme.spacing.lg))
    }
}

@EmPreviews
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
