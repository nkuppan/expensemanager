package com.naveenapps.expensemanager.feature.settings.autobackup

import com.naveenapps.expensemanager.core.model.AutoBackupSettings

data class AutoBackupState(
    /** Null until loaded from storage. */
    val settings: AutoBackupSettings? = null,
    val isBackingUp: Boolean = false,
)
