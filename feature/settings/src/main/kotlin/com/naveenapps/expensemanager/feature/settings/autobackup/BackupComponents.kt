package com.naveenapps.expensemanager.feature.settings.autobackup

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Schedule
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
import com.naveenapps.expensemanager.core.designsystem.em.EmBottomSheet
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmCardVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextField
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
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
    EmCard(
        modifier = modifier.fillMaxWidth(),
        variant = EmCardVariant.Filled,
        onClick = onClick,
        content = content,
    )
}

@Composable
internal fun BackupSectionHeader(text: String) {
    EmText(
        text = text,
        modifier = Modifier.padding(start = EmTheme.spacing.xs, top = EmTheme.spacing.sm),
        role = EmTextRole.SectionTitle,
        color = EmTextColor.Primary,
    )
}

/** An icon on a soft circle, the leading element of the backup rows. */
@Composable
internal fun CircleIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    background: Color = EmTheme.colors.scheme.secondaryContainer,
    tint: Color = EmTheme.colors.scheme.onSecondaryContainer,
) {
    Box(
        modifier = modifier
            .size(EmTheme.icons.tile)
            .background(background, EmTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        EmIcon(icon = icon, contentDescription = null, tint = tint)
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
            .padding(
                start = EmTheme.spacing.lg,
                end = EmTheme.spacing.sm,
                top = EmTheme.spacing.md,
                bottom = EmTheme.spacing.md,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.md),
    ) {
        CircleIcon(icon = if (backup.kind == BackupKind.AUTO) Icons.Outlined.Schedule else Icons.Outlined.Save)
        Column(modifier = Modifier.weight(1f)) {
            EmText(
                text = formatTime(backup.createdAt),
            )
            EmText(
                text = details,
                color = EmTextColor.Supporting,
            )
        }
        trailing()
    }
}

@Composable
internal fun EmptyBackupsText(modifier: Modifier = Modifier) {
    EmText(
        text = stringResource(R.string.no_backups_yet),
        modifier = modifier
            .fillMaxWidth()
            .padding(EmTheme.spacing.xl),
        color = EmTextColor.Supporting,
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
    EmBottomSheet(
        onDismissRequest = onDismiss,
        title = title,
        actions = {
            EmButton(
                text = stringResource(id = android.R.string.cancel),
                onClick = onDismiss,
                variant = EmButtonVariant.Secondary,
                modifier = Modifier.weight(1f),
            )
            EmButton(
                text = confirmLabel,
                onClick = { onSubmit(passphrase) },
                enabled = !isRequired || passphrase.isNotEmpty(),
                modifier = Modifier.weight(1f),
            )
        },
    ) {
        EmText(text = message, color = EmTextColor.Supporting)
        EmTextField(
            value = passphrase,
            onValueChange = { passphrase = it },
            label = stringResource(id = R.string.passphrase_label),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                autoCorrectEnabled = false,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
