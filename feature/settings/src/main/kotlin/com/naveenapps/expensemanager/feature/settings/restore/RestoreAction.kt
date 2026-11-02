package com.naveenapps.expensemanager.feature.settings.restore

import com.naveenapps.expensemanager.core.model.BackupFile

sealed class RestoreAction {

    data object ClosePage : RestoreAction()

    /** Opens the system file picker (Files, Drive, email attachments, ...). */
    data object ChooseFile : RestoreAction()

    /** Result of the file picker; null when the user backed out. */
    data class FileSelected(val uri: String?) : RestoreAction()

    /** Asks to confirm restoring [backup] from the list. */
    data class SelectBackup(val backup: BackupFile) : RestoreAction()

    data object ConfirmRestore : RestoreAction()

    data object DismissConfirmation : RestoreAction()

    data class SubmitPassphrase(val passphrase: String) : RestoreAction()

    data object DismissPassphrase : RestoreAction()

    /** The currency the backup's amounts are in (needed for pre-multi-currency backups). */
    data class ConfirmRestoreCurrency(val currencyCode: String) : RestoreAction()

    data object DismissRestoreCurrency : RestoreAction()
}
