package com.naveenapps.expensemanager.feature.settings.autobackup

import androidx.annotation.StringRes

sealed class AutoBackupEvent {

    /** Open the system folder picker. */
    data object PickFolder : AutoBackupEvent()

    data class ShowMessage(@StringRes val messageResId: Int) : AutoBackupEvent()
}
