package com.naveenapps.expensemanager.feature.settings.restore

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmCardVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmConfirm
import com.naveenapps.expensemanager.core.designsystem.em.EmDivider
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmProgress
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSnackbarHostState
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.feature.settings.R
import com.naveenapps.expensemanager.feature.settings.autobackup.BackupCard
import com.naveenapps.expensemanager.feature.settings.autobackup.BackupFileRow
import com.naveenapps.expensemanager.feature.settings.autobackup.BackupSectionHeader
import com.naveenapps.expensemanager.feature.settings.autobackup.CircleIcon
import com.naveenapps.expensemanager.feature.settings.autobackup.EmptyBackupsText
import com.naveenapps.expensemanager.feature.settings.autobackup.PassphraseDialog
import com.naveenapps.expensemanager.feature.settings.autobackup.rememberBackupTimeFormatter
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RestoreScreen(
    viewModel: RestoreViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { EmSnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Registered from Compose so the result survives Activity recreation.
    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        viewModel.processAction(RestoreAction.FileSelected(uri?.toString()))
    }

    fun showMessage(messageResId: Int) {
        coroutineScope.launch { snackbarHostState.showSnackbar(context.getString(messageResId)) }
    }

    ObserveAsEvents(viewModel.event) {
        when (it) {
            RestoreEvent.PickFile -> try {
                // .aes files have no registered MIME type, so filtering would hide them.
                fileLauncher.launch(arrayOf("*/*"))
            } catch (e: ActivityNotFoundException) {
                showMessage(R.string.no_file_picker)
            }

            is RestoreEvent.ShowMessage -> showMessage(it.messageResId)

            RestoreEvent.RestartApp -> context.restartApp()
        }
    }

    RestoreContent(
        state = state,
        onAction = viewModel::processAction,
        snackbarHostState = snackbarHostState,
    )
}

/** Cold restart so the staged restore is swapped in before the database is opened. */
private fun Context.restartApp() {
    val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return
    startActivity(Intent.makeRestartActivityTask(launchIntent.component))
    Runtime.getRuntime().exit(0)
}

@Composable
private fun RestoreContent(
    state: RestoreState,
    onAction: (RestoreAction) -> Unit,
    snackbarHostState: EmSnackbarHostState = remember { EmSnackbarHostState() },
) {
    val formatTime = rememberBackupTimeFormatter()

    state.confirmBackup?.let { backup ->
        // Restoring replaces everything in the app, so the confirm button is destructive.
        EmConfirm(
            title = stringResource(id = R.string.restore_confirm_title),
            message = stringResource(id = R.string.restore_confirm_backup_message, formatTime(backup.createdAt)),
            confirmText = stringResource(id = R.string.restore_confirm_action),
            onConfirm = { onAction(RestoreAction.ConfirmRestore) },
            onDismiss = { onAction(RestoreAction.DismissConfirmation) },
            destructive = true,
        )
    }

    if (state.showPassphrasePrompt) {
        PassphraseDialog(
            title = stringResource(R.string.restore_passphrase_title),
            message = stringResource(R.string.restore_passphrase_message),
            confirmLabel = stringResource(R.string.restore_confirm_action),
            isRequired = true,
            onSubmit = { onAction(RestoreAction.SubmitPassphrase(it)) },
            onDismiss = { onAction(RestoreAction.DismissPassphrase) },
        )
    }

    EmScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            EmTopBar(
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = { onAction(RestoreAction.ClosePage) },
                title = stringResource(R.string.restore),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = EmTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
        ) {
            if (state.isRestoring) {
                EmProgress(progress = null)
            } else {
                Spacer(modifier = Modifier.height(0.dp))
            }

            RestoreWarning()

            BackupCard(onClick = if (state.isRestoring) null else ({ onAction(RestoreAction.ChooseFile) })) {
                Row(
                    modifier = Modifier.padding(
                        start = EmTheme.spacing.lg,
                        end = EmTheme.spacing.md,
                        top = EmTheme.spacing.lg,
                        bottom = EmTheme.spacing.lg,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.md),
                ) {
                    CircleIcon(icon = Icons.Outlined.Folder)
                    Column(modifier = Modifier.weight(1f)) {
                        EmText(
                            text = stringResource(R.string.restore_choose_file),
                        )
                        EmText(
                            text = stringResource(R.string.restore_choose_file_message),
                            color = EmTextColor.Supporting,
                        )
                    }
                    EmIcon(
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = EmTheme.colors.scheme.onSurfaceVariant,
                    )
                }
            }

            BackupSectionHeader(text = stringResource(R.string.backups_on_device))
            BackupCard {
                if (state.backups.isEmpty()) EmptyBackupsText()
                state.backups.forEachIndexed { index, backup ->
                    if (index > 0) EmDivider(modifier = Modifier.padding(start = 74.dp))
                    BackupFileRow(backup = backup, formatTime = formatTime) {
                        EmButton(
                            text = stringResource(R.string.restore_confirm_action),
                            onClick = { onAction(RestoreAction.SelectBackup(backup)) },
                            variant = EmButtonVariant.Tertiary,
                            enabled = !state.isRestoring,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(EmTheme.spacing.lg))
        }
    }
}

@Composable
private fun RestoreWarning() {
    EmCard(modifier = Modifier.fillMaxWidth(), variant = EmCardVariant.Error) {
        Row(
            modifier = Modifier.padding(EmTheme.spacing.xl),
            horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
        ) {
            EmIcon(icon = Icons.Outlined.WarningAmber, contentDescription = null)
            Column(verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xs)) {
                EmText(
                    text = stringResource(R.string.restore_warning_title),
                    role = EmTextRole.SectionTitle,
                )
                EmText(
                    text = stringResource(R.string.restore_warning_message),
                )
            }
        }
    }
}
