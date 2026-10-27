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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
    val snackbarHostState = remember { SnackbarHostState() }
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
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val formatTime = rememberBackupTimeFormatter()

    state.confirmBackup?.let { backup ->
        AlertDialog(
            onDismissRequest = { onAction(RestoreAction.DismissConfirmation) },
            title = { Text(text = stringResource(id = R.string.restore_confirm_title)) },
            text = { Text(text = stringResource(id = R.string.restore_confirm_backup_message, formatTime(backup.createdAt))) },
            confirmButton = {
                TextButton(onClick = { onAction(RestoreAction.ConfirmRestore) }) {
                    Text(text = stringResource(id = R.string.restore_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(RestoreAction.DismissConfirmation) }) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            },
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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = { onAction(RestoreAction.ClosePage) },
                title = stringResource(R.string.restore),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.isRestoring) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                Spacer(modifier = Modifier.height(0.dp))
            }

            RestoreWarning()

            BackupCard(onClick = if (state.isRestoring) null else ({ onAction(RestoreAction.ChooseFile) })) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    CircleIcon(icon = Icons.Outlined.Folder)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.restore_choose_file),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.restore_choose_file_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            BackupSectionHeader(text = stringResource(R.string.backups_on_device))
            BackupCard {
                if (state.backups.isEmpty()) EmptyBackupsText()
                state.backups.forEachIndexed { index, backup ->
                    if (index > 0) HorizontalDivider(modifier = Modifier.padding(start = 74.dp))
                    BackupFileRow(backup = backup, formatTime = formatTime) {
                        TextButton(
                            onClick = { onAction(RestoreAction.SelectBackup(backup)) },
                            enabled = !state.isRestoring,
                        ) {
                            Text(text = stringResource(R.string.restore_confirm_action))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RestoreWarning() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(imageVector = Icons.Outlined.WarningAmber, contentDescription = null)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.restore_warning_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.restore_warning_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
