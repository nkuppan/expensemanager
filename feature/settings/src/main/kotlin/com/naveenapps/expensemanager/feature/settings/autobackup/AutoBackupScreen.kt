package com.naveenapps.expensemanager.feature.settings.autobackup

import android.content.ActivityNotFoundException
import android.content.ClipData
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Schedule
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
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonSize
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmCardVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmDivider
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton
import com.naveenapps.expensemanager.core.designsystem.em.EmProgress
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSegmented
import com.naveenapps.expensemanager.core.designsystem.em.EmSettingItem
import com.naveenapps.expensemanager.core.designsystem.em.EmSnackbarHostState
import com.naveenapps.expensemanager.core.designsystem.em.EmSwitch
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.tint
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.AutoBackupError
import com.naveenapps.expensemanager.core.model.AutoBackupFrequency
import com.naveenapps.expensemanager.core.model.AutoBackupSettings
import com.naveenapps.expensemanager.core.model.nextAutoBackupAt
import com.naveenapps.expensemanager.core.repository.AutoBackupRepository
import com.naveenapps.expensemanager.feature.settings.R
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AutoBackupScreen(
    viewModel: AutoBackupViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { EmSnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Registered from Compose so the result survives Activity recreation.
    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri: Uri? ->
        viewModel.processAction(AutoBackupAction.FolderSelected(uri?.toString()))
    }

    fun showMessage(messageResId: Int) {
        coroutineScope.launch { snackbarHostState.showSnackbar(context.getString(messageResId)) }
    }

    ObserveAsEvents(viewModel.event) {
        when (it) {
            AutoBackupEvent.PickFolder -> try {
                folderLauncher.launch(null)
            } catch (e: ActivityNotFoundException) {
                showMessage(R.string.no_file_picker)
            }

            is AutoBackupEvent.ShareFile -> context.shareBackup(Uri.parse(it.uri))

            is AutoBackupEvent.ShowMessage -> showMessage(it.messageResId)
        }
    }

    AutoBackupContent(
        state = state,
        onAction = viewModel::processAction,
        snackbarHostState = snackbarHostState,
    )
}

private fun Context.shareBackup(uri: Uri) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = BACKUP_MIME_TYPE
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newRawUri(null, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        startActivity(Intent.createChooser(send, getString(R.string.share_backup)))
    } catch (_: ActivityNotFoundException) {
        // Nothing can receive a file; ignore.
    }
}

private const val BACKUP_MIME_TYPE = "application/octet-stream"

/** How many backups the Backup page lists; Restore lists them all. */
private const val RECENT_BACKUPS = 10

@Composable
private fun AutoBackupContent(
    state: AutoBackupState,
    onAction: (AutoBackupAction) -> Unit,
    snackbarHostState: EmSnackbarHostState = remember { EmSnackbarHostState() },
) {
    state.shareCandidate?.let {
        PassphraseDialog(
            title = stringResource(R.string.share_backup),
            message = stringResource(R.string.share_backup_message),
            confirmLabel = stringResource(R.string.share_action),
            isRequired = false,
            onSubmit = { passphrase -> onAction(AutoBackupAction.SubmitSharePassphrase(passphrase)) },
            onDismiss = { onAction(AutoBackupAction.DismissShare) },
        )
    }

    EmScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            EmTopBar(
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = { onAction(AutoBackupAction.ClosePage) },
                title = stringResource(R.string.backup_and_restore),
            )
        },
    ) { innerPadding ->
        val settings = state.settings ?: return@EmScaffold
        val formatTime = rememberBackupTimeFormatter()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = EmTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
        ) {
            Spacer(modifier = Modifier.height(0.dp))

            BackupStatusCard(
                settings = settings,
                isBackingUp = state.isBackingUp,
                formatTime = formatTime,
                onBackupNow = { onAction(AutoBackupAction.BackupNow) },
            )

            BackupFolderCard(settings = settings, onChange = { onAction(AutoBackupAction.ChooseFolder) })

            AutoBackupCard(
                settings = settings,
                formatTime = formatTime,
                onToggle = { onAction(AutoBackupAction.ToggleEnabled) },
                onSelectFrequency = { onAction(AutoBackupAction.SelectFrequency(it)) },
            )

            BackupSectionHeader(text = stringResource(R.string.recent_backups))
            BackupCard {
                if (state.isPreparingShare) EmProgress(progress = null)
                val recent = state.backups.take(RECENT_BACKUPS)
                if (recent.isEmpty()) EmptyBackupsText()
                recent.forEachIndexed { index, backup ->
                    if (index > 0) EmDivider(modifier = Modifier.padding(start = 74.dp))
                    BackupFileRow(backup = backup, formatTime = formatTime) {
                        EmIconButton(
                            icon = Icons.Outlined.IosShare,
                            contentDescription = stringResource(R.string.share_backup),
                            onClick = { onAction(AutoBackupAction.Share(backup)) },
                            enabled = !state.isPreparingShare,
                        )
                    }
                }
            }

            BackupCard {
                EmSettingItem(
                    icon = Icons.Outlined.Restore,
                    title = stringResource(id = R.string.restore),
                    supporting = stringResource(id = R.string.restore_setting_message),
                    onClick = { onAction(AutoBackupAction.OpenRestore) },
                )
            }

            AboutBackups()

            Spacer(modifier = Modifier.height(EmTheme.spacing.lg))
        }
    }
}

/** "Your data is backed up" / "No backup yet" / "Last backup failed", with Back up now. */
@Composable
private fun BackupStatusCard(
    settings: AutoBackupSettings,
    isBackingUp: Boolean,
    formatTime: (Long) -> String,
    onBackupNow: () -> Unit,
) {
    val error = settings.lastError
    val last = settings.lastBackupAt
    val content = if (error != null) EmTheme.colors.scheme.onErrorContainer else EmTheme.colors.scheme.onPrimaryContainer
    EmCard(
        modifier = Modifier.fillMaxWidth(),
        variant = if (error != null) EmCardVariant.Error else EmCardVariant.Highlight,
    ) {
        Column(
            modifier = Modifier.padding(EmTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xl),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
            ) {
                CircleIcon(
                    icon = when {
                        error != null -> Icons.Outlined.CloudOff
                        last != null -> Icons.Outlined.CloudDone
                        else -> Icons.Outlined.CloudQueue
                    },
                    background = content.tint(),
                    tint = content,
                )
                Column(modifier = Modifier.weight(1f)) {
                    EmText(
                        text = stringResource(
                            when {
                                error != null -> R.string.backup_status_failed
                                last != null -> R.string.backup_status_ok
                                else -> R.string.backup_status_none
                            },
                        ),
                        role = EmTextRole.SectionTitle,
                    )
                    EmText(
                        text = when {
                            error != null -> stringResource(error.messageResId())
                            last != null -> stringResource(R.string.backup_last, formatTime(last))
                            else -> stringResource(R.string.backup_status_none_message)
                        },
                        color = content,
                    )
                }
            }
            EmButton(
                text = stringResource(R.string.auto_backup_now),
                onClick = onBackupNow,
                size = EmButtonSize.Large,
                leadingIcon = Icons.Outlined.CloudUpload,
                loading = isBackingUp,
            )
        }
    }
}

@Composable
private fun BackupFolderCard(settings: AutoBackupSettings, onChange: () -> Unit) {
    BackupCard {
        Row(
            modifier = Modifier.padding(
                start = EmTheme.spacing.xl,
                end = EmTheme.spacing.sm,
                top = EmTheme.spacing.lg,
                bottom = EmTheme.spacing.lg,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
        ) {
            EmIcon(
                icon = Icons.Outlined.Folder,
                contentDescription = null,
                tint = EmTheme.colors.scheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                EmText(
                    text = stringResource(R.string.auto_backup_folder),
                )
                EmText(
                    text = if (settings.folderUri != null) {
                        settings.folderName ?: stringResource(R.string.auto_backup_folder)
                    } else {
                        stringResource(R.string.backup_folder_inside_app)
                    },
                    color = EmTextColor.Supporting,
                )
            }
            EmButton(
                text = stringResource(
                        if (settings.folderUri != null) R.string.backup_folder_change else R.string.backup_folder_choose,
                    ),
                onClick = onChange,
                variant = EmButtonVariant.Tertiary,
            )
        }
    }
}

@Composable
private fun AutoBackupCard(
    settings: AutoBackupSettings,
    formatTime: (Long) -> String,
    onToggle: () -> Unit,
    onSelectFrequency: (AutoBackupFrequency) -> Unit,
) {
    BackupCard {
        Column(
            modifier = Modifier.padding(EmTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
            ) {
                EmIcon(
                    icon = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = EmTheme.colors.scheme.primary,
                )
                Column(modifier = Modifier.weight(1f)) {
                    EmText(
                        text = stringResource(R.string.auto_backup),
                    )
                    EmText(
                        text = stringResource(R.string.auto_backup_subtitle),
                        color = EmTextColor.Supporting,
                    )
                }
                EmSwitch(checked = settings.isEnabled, onCheckedChange = { onToggle() })
            }
            val frequencies = AutoBackupFrequency.entries
            EmSegmented(
                options = frequencies.map { stringResource(it.labelResId()) },
                selectedIndex = frequencies.indexOf(settings.frequency),
                onSelect = { onSelectFrequency(frequencies[it]) },
            )
            if (settings.isEnabled) {
                val next = settings.nextAutoBackupAt(System.currentTimeMillis())
                EmText(
                    text = if (next != null) {
                        stringResource(R.string.auto_backup_next, formatTime(next))
                    } else {
                        stringResource(R.string.auto_backup_next_soon)
                    },
                    color = EmTextColor.Supporting,
                )
            }
        }
    }
}

/** What gets kept and how it's protected, so nobody is surprised by deleted or readable files. */
@Composable
private fun AboutBackups() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
    ) {
        EmIcon(
            icon = Icons.Outlined.Info,
            contentDescription = null,
            size = EmTheme.icons.md,
            tint = EmTheme.colors.scheme.onSurfaceVariant,
        )
        EmText(
            text = stringResource(R.string.auto_backup_info, AutoBackupRepository.KEEP_LAST_BACKUPS),
            role = EmTextRole.Supporting,
            color = EmTextColor.Supporting,
        )
    }
}

private fun AutoBackupError.messageResId(): Int = when (this) {
    AutoBackupError.FOLDER_UNAVAILABLE -> R.string.auto_backup_folder_unavailable
    AutoBackupError.WRITE_FAILED -> R.string.auto_backup_write_failed
}

private fun AutoBackupFrequency.labelResId(): Int = when (this) {
    AutoBackupFrequency.DAILY -> R.string.auto_backup_daily
    AutoBackupFrequency.WEEKLY -> R.string.auto_backup_weekly
    AutoBackupFrequency.MONTHLY -> R.string.auto_backup_monthly
}
