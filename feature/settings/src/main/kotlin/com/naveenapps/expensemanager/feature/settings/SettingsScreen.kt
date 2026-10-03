package com.naveenapps.expensemanager.feature.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.EditNotifications
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.SettingsApplications
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
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
    val state by viewModel.state.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // The pickers are registered from Compose so their results survive Activity recreation
    // (rotation, theme/locale change, or the process being killed while the picker is open).
    // The old backup library registered them in an object that was recreated with no state,
    // and crashed in onResume when the result came back.
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(BACKUP_MIME_TYPE),
    ) { uri: Uri? ->
        viewModel.processAction(SettingAction.BackupDestinationSelected(uri?.toString()))
    }
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        viewModel.processAction(SettingAction.RestoreSourceSelected(uri?.toString()))
    }

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

            is SettingEvent.PickBackupDestination -> {
                try {
                    backupLauncher.launch(it.fileName)
                } catch (e: ActivityNotFoundException) {
                    showMessage(R.string.no_file_picker)
                }
            }

            SettingEvent.PickRestoreSource -> {
                try {
                    // .aes files have no registered MIME type, so filtering would hide them.
                    restoreLauncher.launch(arrayOf("*/*"))
                } catch (e: ActivityNotFoundException) {
                    showMessage(R.string.no_file_picker)
                }
            }

            is SettingEvent.ShowMessage -> {
                showMessage(it.messageResId)
            }

            SettingEvent.RestartApp -> {
                context.restartApp()
            }
        }
    }

    SettingsScreenScaffoldView(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::processAction,
    )
}

private const val BACKUP_MIME_TYPE = "application/octet-stream"

/** Cold restart so the staged restore is swapped in before the database is opened. */
private fun Context.restartApp() {
    val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return
    startActivity(Intent.makeRestartActivityTask(launchIntent.component))
    Runtime.getRuntime().exit(0)
}

@Composable
private fun SettingsScreenScaffoldView(
    state: SettingState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (SettingAction) -> Unit,
) {
    if (state.showRestoreConfirmation) {
        AlertDialog(
            onDismissRequest = { onAction.invoke(SettingAction.DismissRestoreConfirmation) },
            title = { Text(text = stringResource(id = R.string.restore_confirm_title)) },
            text = { Text(text = stringResource(id = R.string.restore_confirm_message)) },
            confirmButton = {
                TextButton(onClick = { onAction.invoke(SettingAction.ConfirmRestore) }) {
                    Text(text = stringResource(id = R.string.restore_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction.invoke(SettingAction.DismissRestoreConfirmation) }) {
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
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(modifier = Modifier.height(0.dp))

        // Personalization — shapes how the app looks and feels, so it comes first.
        SettingsSection(title = stringResource(R.string.personalization)) {
            AppCardView {
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.ShowThemeSelection) },
                    title = stringResource(id = R.string.theme),
                    subtitle = if (state.theme != null) {
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
                    subtitle = if (state.locale != null) {
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
                    subtitle = "${state.currency.name} (${state.currency.symbol})",
                    icon = Icons.Outlined.Payments,
                )
            }
        }

        // Notifications
        SettingsSection(title = stringResource(R.string.notifications)) {
            AppCardView {
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.OpenNotification) },
                    title = stringResource(id = R.string.reminder_notification),
                    subtitle = stringResource(id = R.string.selected_daily_reminder_time),
                    icon = Icons.Outlined.EditNotifications,
                )
            }
        }

        // Defaults — affects daily data entry, so it's promoted out of "Advanced".
        SettingsSection(title = stringResource(R.string.defaults)) {
            AppCardView {
                if (state.accounts.isNotEmpty() && state.selectedAccount != null) {
                    DropdownSettingItem(
                        label = stringResource(id = R.string.default_account),
                        selectedValue = state.selectedAccount.name,
                        icon = Icons.Outlined.AccountBalance,
                        items = state.accounts.map { it.name },
                        onItemSelected = { index ->
                            onAction.invoke(SettingAction.SelectAccount(state.accounts[index]))
                        },
                    )
                }

                if (state.expenseCategories.isNotEmpty() && state.selectedExpenseCategory != null) {
                    DropdownSettingItem(
                        label = stringResource(id = R.string.default_expense_category),
                        selectedValue = state.selectedExpenseCategory.titleResId?.let {
                            stringResource(it)
                        } ?: state.selectedExpenseCategory.name,
                        icon = Icons.AutoMirrored.Outlined.TrendingUp,
                        items = state.expenseCategories.map {
                            it.titleResId?.let { resId -> stringResource(resId) } ?: it.name
                        },
                        onItemSelected = { index ->
                            onAction.invoke(SettingAction.SelectExpenseCategory(state.expenseCategories[index]))
                        },
                    )
                }

                if (state.incomeCategories.isNotEmpty() && state.selectedIncomeCategory != null) {
                    DropdownSettingItem(
                        label = stringResource(id = R.string.default_income_category),
                        selectedValue = state.selectedIncomeCategory.titleResId?.let {
                            stringResource(it)
                        } ?: state.selectedIncomeCategory.name,
                        icon = Icons.AutoMirrored.Outlined.TrendingDown,
                        items = state.incomeCategories.map {
                            it.titleResId?.let { resId -> stringResource(resId) } ?: it.name
                        },
                        onItemSelected = { index ->
                            onAction.invoke(SettingAction.SelectIncomeCategory(state.incomeCategories[index]))
                        },
                    )
                }
                SettingToggleRow(
                    title = stringResource(id = R.string.compact_summary),
                    subtitle = stringResource(id = R.string.compact_summary_message),
                    icon = Icons.Outlined.DashboardCustomize,
                    checked = state.isCompactSummary,
                    onCheckedChange = { onAction.invoke(SettingAction.ToggleCompactSummary) },
                )
            }
        }

        // Data & Backup — export, backup, and restore all live together now (previously split
        // between the main screen and "Advanced").
        SettingsSection(title = stringResource(R.string.data_and_backup)) {
            AppCardView {
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.OpenExport) },
                    title = stringResource(id = R.string.export),
                    subtitle = stringResource(id = R.string.export_message),
                    icon = Icons.Outlined.Upload,
                    showDivider = true,
                )
                SettingRow(
                    title = stringResource(id = R.string.backup),
                    subtitle = stringResource(id = R.string.backup_message),
                    icon = Icons.Outlined.Backup,
                    showDivider = true,
                    onClick = { onAction.invoke(SettingAction.Backup) },
                )
                SettingRow(
                    title = stringResource(id = R.string.restore),
                    subtitle = stringResource(id = R.string.restore_message),
                    icon = Icons.Outlined.Restore,
                    onClick = { onAction.invoke(SettingAction.Restore) },
                )
            }
        }

        // Security
        SettingsSection(title = stringResource(R.string.security)) {
            AppCardView {
                SettingToggleRow(
                    title = stringResource(id = R.string.app_lock),
                    subtitle = stringResource(id = R.string.app_lock_message),
                    icon = Icons.Outlined.Lock,
                    checked = state.isAppLockEnabled,
                    onCheckedChange = { onAction.invoke(SettingAction.ToggleAppLock) },
                )
            }
        }

        // Advanced — only the genuinely rare, set-once-and-forget items live behind this now.
        SettingsSection(title = stringResource(R.string.advanced)) {
            AppCardView {
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.OpenAdvancedSettings) },
                    title = stringResource(id = R.string.advanced),
                    subtitle = stringResource(id = R.string.advanced_config_message),
                    icon = Icons.Outlined.SettingsApplications,
                )
            }
        }

        // Support — informational, so it stays last.
        SettingsSection(title = stringResource(R.string.support)) {
            AppCardView {
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.OpenRateUs) },
                    title = stringResource(id = R.string.rate_us),
                    subtitle = stringResource(id = R.string.rate_us_message),
                    icon = Icons.Outlined.RateReview,
                    showDivider = true,
                )
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.ShareApp) },
                    title = stringResource(id = R.string.share_app),
                    subtitle = stringResource(id = R.string.share_app_message),
                    icon = Icons.Outlined.Share,
                    showDivider = true,
                )
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.OpenAboutUs) },
                    title = stringResource(id = com.naveenapps.expensemanager.feature.about.R.string.about_us),
                    subtitle = stringResource(id = R.string.about_the_app_information),
                    icon = Icons.Outlined.Info,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Moved here from AdvancedSettingsScreen along with the Defaults section it belongs to.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSettingItem(
    label: String,
    selectedValue: String,
    icon: ImageVector,
    items: List<String>,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier.fillMaxWidth(),
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = selectedValue,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = MaterialTheme.shapes.medium,
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item,
                            fontWeight = if (item == selectedValue) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    leadingIcon = if (item == selectedValue) {
                        {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    } else {
                        null
                    },
                    onClick = {
                        onItemSelected(index)
                        expanded = false
                    },
                )
            }
        }
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

/** Play Store listing of the release app (not the ".debug" package a debug build runs as). */
private const val PLAY_STORE_URL =
    "https://play.google.com/store/apps/details?id=com.naveenapps.expensemanager"

/** Word-of-mouth: share a short pitch plus the store link through any app. */
private fun Context.shareApp() {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_text, PLAY_STORE_URL))
    }
    try {
        startActivity(Intent.createChooser(send, getString(R.string.share_app)))
    } catch (_: ActivityNotFoundException) {
        // Nothing can receive a share; ignore.
    }
}
