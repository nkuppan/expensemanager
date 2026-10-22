package com.naveenapps.expensemanager.feature.settings.autobackup

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.common.utils.toTimeAndMinutes
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingRow
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingToggleRow
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingsSection
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.AutoBackupError
import com.naveenapps.expensemanager.core.model.AutoBackupFrequency
import com.naveenapps.expensemanager.core.model.AutoBackupSettings
import com.naveenapps.expensemanager.core.repository.AutoBackupRepository
import com.naveenapps.expensemanager.feature.settings.R
import java.util.Date
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AutoBackupScreen(
    viewModel: AutoBackupViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val resources = LocalContext.current.resources

    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri: Uri? ->
        viewModel.processAction(AutoBackupAction.FolderSelected(uri?.toString()))
    }

    ObserveAsEvents(viewModel.event) {
        when (it) {
            AutoBackupEvent.PickFolder -> folderLauncher.launch(null)

            is AutoBackupEvent.ShowMessage -> coroutineScope.launch {
                snackbarHostState.showSnackbar(resources.getString(it.messageResId))
            }
        }
    }

    AutoBackupContent(
        state = state,
        onAction = viewModel::processAction,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun AutoBackupContent(
    state: AutoBackupState,
    onAction: (AutoBackupAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = { onAction(AutoBackupAction.ClosePage) },
                title = stringResource(R.string.auto_backup),
            )
        },
    ) { innerPadding ->
        val settings = state.settings ?: return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(0.dp))

            AppCardView {
                SettingToggleRow(
                    title = stringResource(R.string.auto_backup),
                    subtitle = stringResource(R.string.auto_backup_toggle_message),
                    icon = Icons.Outlined.Backup,
                    checked = settings.isEnabled,
                    onCheckedChange = { onAction(AutoBackupAction.ToggleEnabled) },
                )
            }

            SettingsSection(title = stringResource(R.string.auto_backup_destination)) {
                AppCardView {
                    SettingRow(
                        title = stringResource(R.string.auto_backup_folder),
                        subtitle = settings.folderName ?: stringResource(R.string.auto_backup_choose_folder),
                        icon = Icons.Outlined.Folder,
                        onClick = { onAction(AutoBackupAction.ChooseFolder) },
                    )
                }
            }

            SettingsSection(title = stringResource(R.string.auto_backup_frequency)) {
                AppCardView {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        AutoBackupFrequency.entries.forEach { frequency ->
                            FilterChip(
                                selected = settings.frequency == frequency,
                                onClick = { onAction(AutoBackupAction.SelectFrequency(frequency)) },
                                label = { Text(text = stringResource(frequency.labelResId())) },
                            )
                        }
                    }
                }
            }

            SettingsSection(title = stringResource(R.string.auto_backup_status)) {
                AppCardView {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        BackupStatus(settings)
                        OutlinedButton(
                            onClick = { onAction(AutoBackupAction.BackupNow) },
                            enabled = settings.folderUri != null && !state.isBackingUp,
                        ) {
                            if (state.isBackingUp) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(ButtonDefaults.IconSize),
                                    strokeWidth = 2.dp,
                                )
                                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                            }
                            Text(text = stringResource(R.string.auto_backup_now))
                        }
                    }
                }
            }

            AboutAutoBackup()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BackupStatus(settings: AutoBackupSettings) {
    val last = settings.lastBackupAt?.let { Date(it) }
    Text(
        text = if (last != null) {
            stringResource(R.string.auto_backup_last, last.toCompleteDateWithDate(), last.toTimeAndMinutes())
        } else {
            stringResource(R.string.auto_backup_never)
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    settings.lastError?.let { error ->
        Text(
            text = stringResource(
                when (error) {
                    AutoBackupError.FOLDER_UNAVAILABLE -> R.string.auto_backup_folder_unavailable
                    AutoBackupError.WRITE_FAILED -> R.string.auto_backup_write_failed
                },
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

/** What gets kept and how it's protected, so nobody is surprised by deleted or readable files. */
@Composable
private fun AboutAutoBackup() {
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

private fun AutoBackupFrequency.labelResId(): Int = when (this) {
    AutoBackupFrequency.DAILY -> R.string.auto_backup_daily
    AutoBackupFrequency.WEEKLY -> R.string.auto_backup_weekly
}
