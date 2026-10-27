package com.naveenapps.expensemanager.feature.settings.restore

import com.naveenapps.expensemanager.core.model.BackupFile

data class RestoreState(
    val backups: List<BackupFile> = emptyList(),
    val isRestoring: Boolean = false,
    /** Non-null while asking to confirm restoring this backup from the list. */
    val confirmBackup: BackupFile? = null,
    /** The chosen backup is passphrase-protected and the passphrase dialog is showing. */
    val showPassphrasePrompt: Boolean = false,
)
