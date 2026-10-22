package com.naveenapps.expensemanager.feature.settings.autobackup

import com.naveenapps.expensemanager.core.model.AutoBackupFrequency

sealed class AutoBackupAction {

    data object ClosePage : AutoBackupAction()

    /** Turning it on without a folder opens the folder picker first. */
    data object ToggleEnabled : AutoBackupAction()

    data object ChooseFolder : AutoBackupAction()

    /** Result of the system folder picker; null when the user backed out. */
    data class FolderSelected(val treeUri: String?) : AutoBackupAction()

    data class SelectFrequency(val frequency: AutoBackupFrequency) : AutoBackupAction()

    data object BackupNow : AutoBackupAction()
}
