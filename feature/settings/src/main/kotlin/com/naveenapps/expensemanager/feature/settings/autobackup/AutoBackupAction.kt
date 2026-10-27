package com.naveenapps.expensemanager.feature.settings.autobackup

import com.naveenapps.expensemanager.core.model.AutoBackupFrequency
import com.naveenapps.expensemanager.core.model.BackupFile

sealed class AutoBackupAction {

    data object ClosePage : AutoBackupAction()

    data object BackupNow : AutoBackupAction()

    data object ChooseFolder : AutoBackupAction()

    /** Result of the system folder picker; null when the user backed out. */
    data class FolderSelected(val treeUri: String?) : AutoBackupAction()

    data object ToggleEnabled : AutoBackupAction()

    data class SelectFrequency(val frequency: AutoBackupFrequency) : AutoBackupAction()

    /** Opens the optional-passphrase dialog before sharing [backup]. */
    data class Share(val backup: BackupFile) : AutoBackupAction()

    /** An empty passphrase shares the backup as it is. */
    data class SubmitSharePassphrase(val passphrase: String) : AutoBackupAction()

    data object DismissShare : AutoBackupAction()
}
