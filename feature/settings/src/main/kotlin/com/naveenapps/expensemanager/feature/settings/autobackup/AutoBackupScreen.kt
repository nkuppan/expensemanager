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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
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
    val snackbarHostState = remember { SnackbarHostState() }
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
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = { onAction(AutoBackupAction.ClosePage) },
                title = stringResource(R.string.backup),
            )
        },
    ) { innerPadding ->
        val settings = state.settings ?: return@Scaffold
        val formatTime = rememberBackupTimeFormatter()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
                if (state.isPreparingShare) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                val recent = state.backups.take(RECENT_BACKUPS)
                if (recent.isEmpty()) EmptyBackupsText()
                recent.forEachIndexed { index, backup ->
                    if (index > 0) HorizontalDivider(modifier = Modifier.padding(start = 74.dp))
                    BackupFileRow(backup = backup, formatTime = formatTime) {
                        IconButton(
                            onClick = { onAction(AutoBackupAction.Share(backup)) },
                            enabled = !state.isPreparingShare,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.IosShare,
                                contentDescription = stringResource(R.string.share_backup),
                            )
                        }
                    }
                }
            }

            AboutBackups()

            Spacer(modifier = Modifier.height(16.dp))
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
    val container = if (error != null) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val content = if (error != null) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = container,
        contentColor = content,
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CircleIcon(
                    icon = when {
                        error != null -> Icons.Outlined.CloudOff
                        last != null -> Icons.Outlined.CloudDone
                        else -> Icons.Outlined.CloudQueue
                    },
                    background = content.copy(alpha = 0.12f),
                    tint = content,
                    modifier = Modifier.size(56.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(
                            when {
                                error != null -> R.string.backup_status_failed
                                last != null -> R.string.backup_status_ok
                                else -> R.string.backup_status_none
                            },
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = when {
                            error != null -> stringResource(error.messageResId())
                            last != null -> stringResource(R.string.backup_last, formatTime(last))
                            else -> stringResource(R.string.backup_status_none_message)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = content.copy(alpha = 0.8f),
                    )
                }
            }
            Button(
                onClick = onBackupNow,
                enabled = !isBackingUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (isBackingUp) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                }
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(text = stringResource(R.string.auto_backup_now))
            }
        }
    }
}

@Composable
private fun BackupFolderCard(settings: AutoBackupSettings, onChange: () -> Unit) {
    BackupCard {
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(imageVector = Icons.Outlined.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.auto_backup_folder),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (settings.folderUri != null) {
                        settings.folderName ?: stringResource(R.string.auto_backup_folder)
                    } else {
                        stringResource(R.string.backup_folder_inside_app)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onChange) {
                Text(
                    text = stringResource(
                        if (settings.folderUri != null) R.string.backup_folder_change else R.string.backup_folder_choose,
                    ),
                )
            }
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
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(imageVector = Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.auto_backup),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.auto_backup_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = settings.isEnabled, onCheckedChange = { onToggle() })
            }
            val frequencies = AutoBackupFrequency.entries
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                frequencies.forEachIndexed { index, frequency ->
                    SegmentedButton(
                        selected = settings.frequency == frequency,
                        onClick = { onSelectFrequency(frequency) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = frequencies.size),
                        icon = {},
                        label = { Text(text = stringResource(frequency.labelResId())) },
                    )
                }
            }
            if (settings.isEnabled) {
                val next = settings.nextAutoBackupAt(System.currentTimeMillis())
                Text(
                    text = if (next != null) {
                        stringResource(R.string.auto_backup_next, formatTime(next))
                    } else {
                        stringResource(R.string.auto_backup_next_soon)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = stringResource(R.string.auto_backup_info, AutoBackupRepository.KEEP_LAST_BACKUPS),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
