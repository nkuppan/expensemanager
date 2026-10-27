package com.naveenapps.expensemanager.feature.settings.autobackup

import android.text.format.Formatter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.model.BackupFile
import com.naveenapps.expensemanager.core.model.BackupKind
import com.naveenapps.expensemanager.feature.settings.R
import java.text.DateFormat
import java.util.Date

/** "Oct 27, 2026, 10:02 AM" in the user's locale. */
@Composable
internal fun rememberBackupTimeFormatter(): (Long) -> String {
    val locale = LocalConfiguration.current.locales[0]
    val format = remember(locale) { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale) }
    return { millis -> format.format(Date(millis)) }
}

/** A borderless rounded card, as used throughout the Backup and Restore pages. */
@Composable
internal fun BackupCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppCardView(
        modifier = modifier.fillMaxWidth(),
        cornerSize = 20.dp,
        border = BorderStroke(0.dp, Color.Transparent),
        onClick = onClick,
        content = content,
    )
}

@Composable
internal fun BackupSectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp),
    )
}

/** An icon on a soft circle, the leading element of the backup rows. */
@Composable
internal fun CircleIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.secondaryContainer,
    tint: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .background(background, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
    }
}

/** One backup: when it was made, manual or automatic, its size and where it lives. */
@Composable
internal fun BackupFileRow(
    backup: BackupFile,
    formatTime: (Long) -> String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val details = buildList {
        add(stringResource(if (backup.kind == BackupKind.AUTO) R.string.backup_kind_auto else R.string.backup_kind_manual))
        add(Formatter.formatShortFileSize(context, backup.sizeBytes))
        if (backup.isInsideApp) add(stringResource(R.string.backup_inside_app))
    }.joinToString(" · ")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CircleIcon(icon = if (backup.kind == BackupKind.AUTO) Icons.Outlined.Schedule else Icons.Outlined.Save)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = formatTime(backup.createdAt),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = details,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        trailing()
    }
}

@Composable
internal fun EmptyBackupsText(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.no_backups_yet),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
    )
}

/** Asks for a passphrase; [isRequired] disables the confirm button until one is typed. */
@Composable
internal fun PassphraseDialog(
    title: String,
    message: String,
    confirmLabel: String,
    isRequired: Boolean,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var passphrase by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column {
                Text(text = message)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = passphrase,
                    onValueChange = { passphrase = it },
                    label = { Text(text = stringResource(id = R.string.passphrase_label)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        autoCorrectEnabled = false,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(passphrase) },
                enabled = !isRequired || passphrase.isNotEmpty(),
            ) {
                Text(text = confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
    )
}
