package com.naveenapps.expensemanager.feature.settings

import androidx.annotation.StringRes

sealed class SettingEvent {

    data object RateUs : SettingEvent()

    data object ShareApp : SettingEvent()

    // Moved here from AdvancedSettingEvent along with the Data & Backup section.

    /** Open the system "create document" picker with [fileName] suggested. */
    data class PickBackupDestination(val fileName: String) : SettingEvent()

    /** Open the system "open document" picker. */
    data object PickRestoreSource : SettingEvent()

    data class ShowMessage(@StringRes val messageResId: Int) : SettingEvent()

    /** A restore was staged; restarting swaps it in before the database is opened. */
    data object RestartApp : SettingEvent()
}
