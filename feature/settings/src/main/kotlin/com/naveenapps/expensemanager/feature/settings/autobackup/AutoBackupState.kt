package com.naveenapps.expensemanager.feature.settings.autobackup

import com.naveenapps.expensemanager.core.model.AutoBackupSettings
import com.naveenapps.expensemanager.core.model.BackupFile

/** State of the Backup page: status, folder, schedule and recent backups. */
data class AutoBackupState(
    /** Null until loaded from storage. */
    val settings: AutoBackupSettings? = null,
    val backups: List<BackupFile> = emptyList(),
    val isBackingUp: Boolean = false,
    /** Non-null while the "share with a passphrase?" dialog is showing for this backup. */
    val shareCandidate: BackupFile? = null,
    val isPreparingShare: Boolean = false,
)
